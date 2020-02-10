package pt.pakenuh.hollywood.sandbox.actor

import kotlinx.coroutines.delay
import pt.pak3nuh.hollywood.actor.proxy.ActorProxy
import pt.pak3nuh.hollywood.processor.Actor
import pt.pakenuh.hollywood.sandbox.clinic.Exam
import pt.pakenuh.hollywood.sandbox.clinic.OwnerContactResult
import pt.pakenuh.hollywood.sandbox.pet.Pet
import pt.pakenuh.hollywood.sandbox.vet.Vet
import java.util.LinkedList
import java.util.Queue

@Actor
interface VetActor {
    suspend fun trySchedule(pet: Pet): Boolean
    /**
     * Starts the actor on an endless loop
     */
    suspend fun startWork(maxSlots: Int): Nothing
}

class VetProxy(override val delegate: VetActor, override val actorId: String) : ActorProxy<VetActor>, VetActor by delegate

class VetFactory(private val actors: ClinicActors) : FactoryBase<VetActor, VetProxy>(VetActor::class, VetProxy::class, ::VetProxy) {
    fun createVet(vet: Vet): VetActor = VetActorImpl(vet, actors)
}

private class VetActorImpl(private val vet: Vet, private val actors: ClinicActors) : VetActor {

    private val slots: Queue<Pet> = LinkedList()
    private var maxSlots = 0

    override suspend fun trySchedule(pet: Pet): Boolean {
        return if (slots.size == MAX_SCHEDULE) {
            false
        } else {
            slots.offer(pet)
        }
    }

    override suspend fun startWork(maxSlots: Int): Nothing {
        this.maxSlots = maxSlots
        while (true) {
            val pet: Pet? = slots.poll()
            if (pet == null) {
                delay(DELAY_TIME_MS)
            } else {
                checkPet(pet)
            }
        }
    }

    private suspend fun checkPet(pet: Pet) {
        val clinic = actors.getClinic()
        if (!isHealthy(pet)) {
            // for simplicity only the first result is obtained
            val (exam, treatment) = when {
                pet.brokenBones -> clinic.orderExam(pet, Exam.X_RAY) to Treatment.APPLY_CAST
                pet.getsTiredFast -> clinic.orderExam(pet, Exam.SOUND_SCAN) to Treatment.HEART_CIRGURY
                pet.hasFainted -> clinic.orderExam(pet, Exam.BLOOD_PRESSURE) to Treatment.BLOOD_THINNER
                pet.hasSkinRash -> clinic.orderExam(pet, Exam.ALLERGY_TEST) to Treatment.CORTIZONE
                pet.peesBlood -> clinic.orderExam(pet, Exam.KIDNEY_EXAM) to Treatment.KIDNEY_TRANSPLANT
                else -> error("shouldn't be here")
            }
            val owner = actors.getOwner(pet.petId.ownerId)
            when (owner.contact(exam, treatment)) {
                OwnerContactResult.APPLY_TREATMENT -> {
                    actors.getPet(pet).applyTreatment(treatment)
                }
                OwnerContactResult.NO_ACTION -> {
                }
            }
        }
        clinic.petReady(pet)
    }

    private fun isHealthy(pet: Pet): Boolean {
        return !pet.peesBlood && !pet.brokenBones && !pet.getsTiredFast && !pet.hasFainted && !pet.hasSkinRash
    }

    private companion object {
        private const val MAX_SCHEDULE = 5
        private const val DELAY_TIME_MS = 1_000L
    }
}
