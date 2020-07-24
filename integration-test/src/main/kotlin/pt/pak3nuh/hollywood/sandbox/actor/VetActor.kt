package pt.pak3nuh.hollywood.sandbox.actor

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import pt.pak3nuh.hollywood.processor.Actor
import pt.pak3nuh.hollywood.sandbox.Loggers
import pt.pak3nuh.hollywood.sandbox.clinic.Exam
import pt.pak3nuh.hollywood.sandbox.clinic.ExamResult
import pt.pak3nuh.hollywood.sandbox.clinic.OwnerContactResult
import pt.pak3nuh.hollywood.sandbox.pet.Pet
import kotlin.coroutines.coroutineContext

@Actor
interface VetActor {
    suspend fun trySchedule(pet: Pet): Boolean
    /**
     * Starts the actor on an endless loop
     */
    suspend fun startWork()
}

class VetFactory(private val actors: ClinicActors, private val maxSlots: Int) : VetActorBaseFactory {
    fun createVet(): VetActor = VetActorImpl(actors, maxSlots)
}

private class VetActorImpl(private val actors: ClinicActors, private val maxSlots: Int) : VetActor {

    private val slots: Channel<Pet> = Channel(maxSlots)
    private var currSlots = 0
    private val logger = Loggers.getLogger<VetActorImpl>()

    override suspend fun trySchedule(pet: Pet): Boolean {
        logger.info("Scheduling pet ${pet.petId.name}")
        return if (currSlots == maxSlots) {
            logger.fine("Can't schedule more pets")
            false
        } else {
            logger.fine("Pet scheduled")
            currSlots++
            slots.offer(pet)
        }
    }

    override suspend fun startWork() {
        logger.info("Starting vet loop")
        while (coroutineContext.isActive) {
            logger.fine("Getting pet from inbox")
            val pet: Pet = slots.receive()
            checkPet(pet)
        }
        logger.info("Exiting vet loop")
    }

    private suspend fun checkPet(pet: Pet) {
        logger.info("Start checking pet ${pet.petId.name}")
        val clinic = actors.getClinic()
        if (!isHealthy(pet)) {
            // for simplicity only the first result is obtained
            logger.fine("Pet unhealthy, starting analysis")
            val result: Any = when {
                pet.brokenBones -> clinic.orderExam(pet, Exam.X_RAY) to Treatment.APPLY_CAST
                pet.getsTiredFast -> clinic.orderExam(pet, Exam.SOUND_SCAN) to Treatment.HEART_CIRGURY
                pet.hasFainted -> clinic.orderExam(pet, Exam.BLOOD_PRESSURE) to Treatment.BLOOD_THINNER
                pet.hasSkinRash -> clinic.orderExam(pet, Exam.ALLERGY_TEST) to Treatment.CORTIZONE
                pet.peesBlood -> clinic.orderExam(pet, Exam.KIDNEY_EXAM) to Treatment.KIDNEY_TRANSPLANT
                else -> error("shouldn't be here")
            }
            val (exam, treatment) = (result as Pair<ExamResult, Treatment>)
            logger.fine("Analysis result $exam contacting owner")
            val owner = actors.getOwner(pet.petId.ownerId)
            when (owner.contact(exam, treatment)) {
                OwnerContactResult.APPLY_TREATMENT -> {
                    logger.fine("Applying treatment")
                    actors.getPet(pet).applyTreatment(treatment)
                }
                OwnerContactResult.NO_ACTION -> {
                    logger.fine("No treatment applied")
                }
            }
        }
    }

    private fun isHealthy(pet: Pet): Boolean {
        return !pet.peesBlood && !pet.brokenBones && !pet.getsTiredFast && !pet.hasFainted && !pet.hasSkinRash
    }
}
