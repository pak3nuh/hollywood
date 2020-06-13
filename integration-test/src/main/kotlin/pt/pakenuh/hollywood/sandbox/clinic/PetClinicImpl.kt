package pt.pakenuh.hollywood.sandbox.clinic

import kotlinx.coroutines.launch
import pt.pak3nuh.hollywood.system.ActorSystem
import pt.pakenuh.hollywood.sandbox.Loggers
import pt.pakenuh.hollywood.sandbox.actor.VetFactory
import pt.pakenuh.hollywood.sandbox.actor.getPetClinic
import pt.pakenuh.hollywood.sandbox.owner.ContactService
import pt.pakenuh.hollywood.sandbox.owner.CreditCard
import pt.pakenuh.hollywood.sandbox.owner.OwnerContacts
import pt.pakenuh.hollywood.sandbox.pet.Pet
import pt.pakenuh.hollywood.sandbox.pet.PetId
import pt.pakenuh.hollywood.sandbox.vet.Vet

class PetClinicImpl(
        actorSystem: ActorSystem,
        vets: List<Vet>,
        private val contactService: ContactService
) : PetClinic {

    private val petClinicActor = actorSystem.getPetClinic()
    private val logger = Loggers.getLogger<PetClinicImpl>()
    override val actorScope = actorSystem.actorScope

    init {
        vets.asSequence().map { vet ->
            actorSystem.actorManager.getOrCreateActor(vet.name, VetFactory::class) {
                logger.info("Creating vet ${vet.name}")
                it.createVet()
            }
        }.forEach {
            actorScope.launch {
                logger.fine("Starting vet actor")
                it.startWork()
            }
        }
    }

    override suspend fun checkinPet(pet: Pet, contacts: OwnerContacts) {
        contactService.registerContact(pet.petId.ownerId, contacts)
        actorScope.launch {
            petClinicActor.checkinPet(pet)
        }
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
    }
}
