package pt.pak3nuh.hollywood.sandbox.actor

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import pt.pak3nuh.hollywood.actor.proxy.ActorScope
import pt.pak3nuh.hollywood.processor.Actor
import pt.pak3nuh.hollywood.sandbox.Loggers
import pt.pak3nuh.hollywood.sandbox.clinic.Exam
import pt.pak3nuh.hollywood.sandbox.clinic.ExamResult
import pt.pak3nuh.hollywood.sandbox.clinic.OwnerContactResult
import pt.pak3nuh.hollywood.sandbox.pet.Pet
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.suspendCoroutine

@Actor
interface VetActor {
    suspend fun trySchedule(pet: Pet): Boolean
}

class VetFactory(
        private val actors: ClinicActors,
        private val maxSlots: Int,
        private val actorScope: ActorScope
) : VetActorBaseFactory {
    fun createVet(): VetActor = VetActorImpl(actors, maxSlots, actorScope)
}

private class VetActorImpl(
        private val actors: ClinicActors,
        private val maxSlots: Int,
        private val actorScope: ActorScope
) : VetActor {

    private val slots: Channel<Pet> = Channel(maxSlots)
    private var currSlots = 0
    private val logger = Loggers.getLogger<VetActorImpl>()
    private var started = false

    override suspend fun trySchedule(pet: Pet): Boolean {
        logger.info("Scheduling pet ${pet.petId.name}")
        if (!started) {
            startWork()
        }
        return if (currSlots == maxSlots) {
            logger.debug("Can't schedule more pets")
            false
        } else {
            logger.debug("Pet scheduled")
            currSlots++
            slots.offer(pet)
        }
    }

    suspend fun startWork() {
        actorScope.launch {
            logger.info("Starting vet loop")
            while (this.isActive) {
                logger.debug("Getting pet from inbox")
                val pet: Pet = slots.receive()
                checkPet(pet)
            }
            logger.info("Exiting vet loop")
        }
    }

    private suspend fun checkPet(pet: Pet) {
        logger.info("Start checking pet ${pet.petId.name}")
        val clinic = actors.getClinic()
        if (!isHealthy(pet)) {
            // for simplicity only the first result is obtained
            logger.debug("Pet unhealthy, starting analysis")
            val result: Any = when {
                pet.brokenBones -> clinic.orderExam(pet, Exam.X_RAY) to Treatment.APPLY_CAST
                pet.getsTiredFast -> clinic.orderExam(pet, Exam.SOUND_SCAN) to Treatment.HEART_CIRGURY
                pet.hasFainted -> clinic.orderExam(pet, Exam.BLOOD_PRESSURE) to Treatment.BLOOD_THINNER
                pet.hasSkinRash -> clinic.orderExam(pet, Exam.ALLERGY_TEST) to Treatment.CORTIZONE
                pet.peesBlood -> clinic.orderExam(pet, Exam.KIDNEY_EXAM) to Treatment.KIDNEY_TRANSPLANT
                else -> error("shouldn't be here")
            }
            val (exam, treatment) = (result as Pair<ExamResult, Treatment>)
            logger.debug("Analysis result $exam contacting owner")
            val owner = actors.getOwner(pet.petId.ownerId)
            when (owner.contact(exam, treatment)) {
                OwnerContactResult.APPLY_TREATMENT -> {
                    logger.debug("Applying treatment")
                    actors.getPet(pet).applyTreatment(treatment)
                }
                OwnerContactResult.NO_ACTION -> {
                    logger.debug("No treatment applied")
                }
            }
        }
    }

    private fun isHealthy(pet: Pet): Boolean {
        return !pet.peesBlood && !pet.brokenBones && !pet.getsTiredFast && !pet.hasFainted && !pet.hasSkinRash
    }
}
