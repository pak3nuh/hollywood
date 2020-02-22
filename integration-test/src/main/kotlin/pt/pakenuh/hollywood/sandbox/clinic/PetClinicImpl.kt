package pt.pakenuh.hollywood.sandbox.clinic

import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import pt.pak3nuh.hollywood.system.ActorSystem
import pt.pakenuh.hollywood.sandbox.Loggers
import pt.pakenuh.hollywood.sandbox.actor.OwnerContacts
import pt.pakenuh.hollywood.sandbox.actor.VetFactory
import pt.pakenuh.hollywood.sandbox.actor.getPetClinic
import pt.pakenuh.hollywood.sandbox.owner.CreditCard
import pt.pakenuh.hollywood.sandbox.pet.Pet
import pt.pakenuh.hollywood.sandbox.pet.PetId
import pt.pakenuh.hollywood.sandbox.vet.Vet
import kotlin.coroutines.CoroutineContext

class PetClinicImpl(actorSystem: ActorSystem, vets: List<Vet>) : PetClinic, CoroutineScope {

    private val job: CompletableJob = Job()
    private val petClinicActor = actorSystem.getPetClinic()
    override val coroutineContext: CoroutineContext = job
    private val logger = Loggers.getLogger<PetClinicImpl>()

    init {
        vets.forEach { vet ->
            val actor = actorSystem.actorManager.getOrCreateActor(vet.name, VetFactory::class) {
                it.createVet(vet)
            }
            launch(Dispatchers.Default) {
                logger.info("Starting vet ${vet.name}")
                actor.startWork()
            }
        }
    }

    override suspend fun checkinPet(pet: Pet, contacts: OwnerContacts) {
        petClinicActor.checkinPet(pet, contacts)
    }

    override suspend fun checkoutPet(petId: PetId, creditCard: CreditCard): Receipt {
        return petClinicActor.checkoutPet(petId, creditCard)
    }

    override suspend fun seePet(petId: PetId, actions: (Pet) -> Unit) {
        val pet = petClinicActor.getPetToSee(petId)
        actions(pet)
    }

    override suspend fun currentPets(): List<PetId> {
        return petClinicActor.getPets()
    }

    override suspend fun waitClosing() {
        petClinicActor.waitClosing()
        job.complete()
    }
}
