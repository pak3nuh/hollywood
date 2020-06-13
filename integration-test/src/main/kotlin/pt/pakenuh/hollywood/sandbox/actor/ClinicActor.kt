package pt.pakenuh.hollywood.sandbox.actor

import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Job
import pt.pak3nuh.hollywood.actor.ActorFactory
import pt.pak3nuh.hollywood.actor.proxy.ActorProxyBase
import pt.pak3nuh.hollywood.actor.proxy.ProxyConfiguration
import pt.pak3nuh.hollywood.processor.Actor
import pt.pak3nuh.hollywood.system.ActorSystem
import pt.pakenuh.hollywood.sandbox.Loggers
import pt.pakenuh.hollywood.sandbox.PetClinicException
import pt.pakenuh.hollywood.sandbox.actor.proxy.ClinicBinaryProxy
import pt.pakenuh.hollywood.sandbox.clinic.Exam
import pt.pakenuh.hollywood.sandbox.clinic.ExamResult
import pt.pakenuh.hollywood.sandbox.clinic.NokResult
import pt.pakenuh.hollywood.sandbox.clinic.OkResult
import pt.pakenuh.hollywood.sandbox.clinic.Receipt
import pt.pakenuh.hollywood.sandbox.owner.CreditCard
import pt.pakenuh.hollywood.sandbox.pet.Pet
import pt.pakenuh.hollywood.sandbox.pet.PetId
import pt.pakenuh.hollywood.sandbox.vet.Vet
import kotlin.reflect.KClass

abstract class CustomProxy<T>(delegate: T, config: ProxyConfiguration) : ActorProxyBase<T>(delegate, config)

@Actor(CustomProxy::class)
interface ClinicActor {
    suspend fun checkinPet(pet: Pet)
    suspend fun checkoutPet(petId: PetId, creditCard: CreditCard): Receipt
    suspend fun petReady(pet: Pet)
    suspend fun orderExam(pet: Pet, exam: Exam): ExamResult
    suspend fun getPetToSee(petId: PetId): Pet
    suspend fun getPets(): List<PetId>
    suspend fun waitClosing()

    companion object {
        const val CLINIC_ID = "clinic unique id"
    }
}

class ClinicFactory(private val vets: List<Vet>, private val actors: ClinicActors, val parentJob: Job) : ClinicActorBaseFactory {
    fun createClinic(): ClinicActor = ClinicActorImpl(vets, actors, parentJob)
}

class ClinicBinaryFactory(private val vets: List<Vet>, private val actors: ClinicActors, val parentJob: Job) : ActorFactory<ClinicActor, ClinicBinaryProxy> {
    override fun createProxy(delegate: ClinicActor, config: ProxyConfiguration): ClinicBinaryProxy = ClinicBinaryProxy(delegate, config)
    override val actorKClass: KClass<ClinicActor> = ClinicActor::class
    override val proxyKClass: KClass<ClinicBinaryProxy> = ClinicBinaryProxy::class

    fun createClinic(): ClinicActor = ClinicActorImpl(vets, actors, parentJob)
}

internal class ClinicActorImpl(private val vets: List<Vet>, private val actors: ClinicActors, parentJob: Job) : ClinicActor {

    private val pets = mutableMapOf<String, PetInObservation>()
    private val job: CompletableJob = Job(parentJob)
    private val logger = Loggers.getLogger<ClinicActorImpl>()

    override suspend fun checkinPet(pet: Pet) {
        val firstAvailableVet = vets.firstOrNull {
            actors.getVet(it).trySchedule(pet)
        } ?: throw PetClinicException("No vets available")

        pets[pet.petId.registryId] = PetInObservation(pet, firstAvailableVet)
        return
    }

    override suspend fun petReady(pet: Pet) {
        val owner = actors.getOwner(pet.petId.ownerId)
        requirePet(pet.petId).state = PetInObservation.State.READY
        owner.petReady()
    }

    override suspend fun orderExam(pet: Pet, exam: Exam): ExamResult {
        requirePet(pet.petId).exams.add(exam)
        Randoms.delay(1_000)
        return if (Randoms.bool()) OkResult else NokResult
    }

    override suspend fun checkoutPet(petId: PetId, creditCard: CreditCard): Receipt {
        logger.info("Checking out pet")
        val petInObservation = requirePet(petId)
        if (petInObservation.state != PetInObservation.State.READY) {
            throw PetClinicException("Pet not ready")
        }
        logger.fine("Issuing receipt")
        val receipt = issueReceipt(creditCard, petInObservation)
        pets.remove(petId.registryId)
        if (pets.isEmpty()) {
            job.complete()
        }
        logger.fine("Pet checked out")
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

    private fun requirePet(petId: PetId): PetInObservation = pets[petId.registryId]
            ?: throw PetClinicException("Unknown pet")
}

private data class PetInObservation(
        val pet: Pet,
        val vet: Vet,
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
