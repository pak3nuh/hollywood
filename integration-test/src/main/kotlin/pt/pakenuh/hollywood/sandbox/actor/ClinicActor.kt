package pt.pakenuh.hollywood.sandbox.actor

import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Job
import pt.pak3nuh.hollywood.actor.proxy.ActorProxy
import pt.pak3nuh.hollywood.processor.Actor
import pt.pak3nuh.hollywood.system.ActorSystem
import pt.pakenuh.hollywood.sandbox.PetClinicException
import pt.pakenuh.hollywood.sandbox.clinic.Exam
import pt.pakenuh.hollywood.sandbox.clinic.ExamResult
import pt.pakenuh.hollywood.sandbox.clinic.NokResult
import pt.pakenuh.hollywood.sandbox.clinic.OkResult
import pt.pakenuh.hollywood.sandbox.clinic.Receipt
import pt.pakenuh.hollywood.sandbox.owner.CreditCard
import pt.pakenuh.hollywood.sandbox.owner.OwnerId
import pt.pakenuh.hollywood.sandbox.pet.Pet
import pt.pakenuh.hollywood.sandbox.pet.PetId
import pt.pakenuh.hollywood.sandbox.vet.Vet

@Actor
interface ClinicActor {
    suspend fun checkinPet(pet: Pet, contacts: OwnerContacts)
    suspend fun checkoutPet(petId: PetId, creditCard: CreditCard): Receipt
    suspend fun petReady(pet: Pet)
    suspend fun orderExam(pet: Pet, exam: Exam): ExamResult
    suspend fun getPetToSee(petId: PetId): Pet
    suspend fun getPets(): List<PetId>
    suspend fun waitClosing()
    suspend fun getOwnerContact(ownerId: OwnerId): OwnerContacts

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
    private val job: CompletableJob = Job()

    override suspend fun checkinPet(pet: Pet, contacts: OwnerContacts) {
        vets.forEach {
            if (actors.getVet(it).trySchedule(pet)) {
                pets[pet.petId.registryId] = PetInObservation(pet, contacts)
                return
            }
        }
        throw PetClinicException("No vets available")
    }

    override suspend fun petReady(pet: Pet) {
        val owner = actors.getOwner(pet.petId.ownerId)
        pets.getValue(pet.petId.registryId).state = PetInObservation.State.READY
        owner.petReady()
    }

    override suspend fun orderExam(pet: Pet, exam: Exam): ExamResult {
        pets.getValue(pet.petId.registryId).exams.add(exam)
        Randoms.delay(1_000)
        return if (Randoms.bool()) OkResult else NokResult
    }

    override suspend fun checkoutPet(petId: PetId, creditCard: CreditCard): Receipt {
        val petInObservation = requirePet(petId)
        if (petInObservation.state != PetInObservation.State.READY) {
            throw PetClinicException("Pet not ready")
        }
        val receipt = issueReceipt(creditCard, petInObservation)
        pets.remove(petId.registryId)
        if (pets.isEmpty()) {
            job.complete()
        }
        return receipt
    }

    private fun issueReceipt(creditCard: CreditCard, petInObservation: PetInObservation): Receipt {
        val total = petInObservation.exams.sumBy { it.cost } + petInObservation.treatments.sumBy { it.cost }
        if (total > creditCard.plafond) {
            throw PetClinicException("Insufficient funds")
        }
        return Receipt(petInObservation.pet.petId, total)
    }

    override suspend fun getPetToSee(petId: PetId): Pet {
        val petInObservation = requirePet(petId)
        if (!petInObservation.state.canBeSeen) {
            throw PetClinicException("Pet in ${petInObservation.state}, can't be seen.")
        }
        return petInObservation.pet
    }

    override suspend fun getPets(): List<PetId> {
        return pets.map { it.value.pet.petId }
    }

    override suspend fun waitClosing() {
        job.join()
    }

    override suspend fun getOwnerContact(ownerId: OwnerId): OwnerContacts {
        return pets.asSequence()
                .filter { it.value.pet.petId.ownerId == ownerId }
                .map { it.value.contact }
                .first()
    }

    private fun requirePet(petId: PetId): PetInObservation = pets[petId.registryId]
            ?: throw PetClinicException("Unknown pet")
}

private data class PetInObservation(
        val pet: Pet,
        val contact: OwnerContacts,
        var state: State = State.WAITING_VET,
        val exams: MutableList<Exam> = ArrayList(),
        val treatments: MutableList<Treatment> = ArrayList()
) {
    enum class State(val canBeSeen: Boolean) {
        WAITING_VET(true),
        IN_OBSERVATION(false),
        WAITING_TREATMENT(true),
        IN_TREATMENT(true),
        READY(false)
    }
}

internal fun ActorSystem.getPetClinic(): ClinicActor =
        actorManager.getOrCreateActor(ClinicActor.CLINIC_ID, ClinicFactory::class, ClinicFactory::createClinic)
