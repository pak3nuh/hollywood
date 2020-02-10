package pt.pakenuh.hollywood.sandbox.actor

import pt.pak3nuh.hollywood.actor.proxy.ActorProxy
import pt.pak3nuh.hollywood.processor.Actor
import pt.pak3nuh.hollywood.system.ActorSystem
import pt.pakenuh.hollywood.sandbox.PetClinicException
import pt.pakenuh.hollywood.sandbox.clinic.Exam
import pt.pakenuh.hollywood.sandbox.clinic.ExamResult
import pt.pakenuh.hollywood.sandbox.clinic.NokResult
import pt.pakenuh.hollywood.sandbox.clinic.OkResult
import pt.pakenuh.hollywood.sandbox.owner.CreditCard
import pt.pakenuh.hollywood.sandbox.pet.Pet
import pt.pakenuh.hollywood.sandbox.pet.PetId
import pt.pakenuh.hollywood.sandbox.vet.Vet

@Actor
interface ClinicActor {
    suspend fun checkinPet(pet: Pet)
    suspend fun checkoutPet(petId: PetId, creditCard: CreditCard)
    suspend fun petReady(pet: Pet)
    suspend fun orderExam(pet: Pet, exam: Exam): ExamResult

    companion object {
        const val CLINIC_ID = "clinic unique id"
    }
}

class ClinicProxy(override val delegate: ClinicActor, override val actorId: String) : ActorProxy<ClinicActor>, ClinicActor by delegate

class ClinicFactory(private val vets: List<Vet>, private val actors: ClinicActors) :
        FactoryBase<ClinicActor, ClinicProxy>(ClinicActor::class, ClinicProxy::class, ::ClinicProxy) {
    fun createClinic(): ClinicActor = ClinicActorImpl(vets, actors)
}

internal class ClinicActorImpl(private val vets: List<Vet>, private val actors: ClinicActors) : ClinicActor {

    private val pets = mutableMapOf<String, PetInObservation>()

    override suspend fun checkinPet(pet: Pet) {
        vets.forEach {
            if (actors.getVet(it).trySchedule(pet)) {
                pets[pet.petId.registryId] = PetInObservation(pet)
                return
            }
        }
        throw PetClinicException("No vets available")
    }

    override suspend fun petReady(pet: Pet) {
        val owner = actors.getOwner(pet.petId.ownerId)
        pets.getValue(pet.petId.registryId).ready = true
        owner.petReady()
    }

    override suspend fun orderExam(pet: Pet, exam: Exam): ExamResult {
        pets.getValue(pet.petId.registryId).exams.add(exam)
        Randoms.delay(1_000)
        return if (Randoms.bool()) OkResult else NokResult
    }

    override suspend fun checkoutPet(petId: PetId, creditCard: CreditCard) {
        val petInObservation = pets[petId.registryId] ?: throw PetClinicException("Unknown pet")
        if (!petInObservation.ready) {
            throw PetClinicException("Pet not ready")
        }
        val total = petInObservation.exams.sumBy { it.cost } + petInObservation.treatments.sumBy { it.cost }
        if (total > creditCard.plafond) {
            throw PetClinicException("Insufficient funds")
        }
        pets.remove(petId.registryId)
    }
}

private data class PetInObservation(
        val pet: Pet,
        var ready: Boolean = false,
        val exams: MutableList<Exam> = ArrayList(),
        val treatments: MutableList<Treatment> = ArrayList()
)

internal fun ActorSystem.getPetClinic(): ClinicActor =
        actorManager.getOrCreateActor(ClinicActor.CLINIC_ID, ClinicFactory::class, ClinicFactory::createClinic)
