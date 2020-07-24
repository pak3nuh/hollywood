package pt.pak3nuh.hollywood.sandbox.clinic

import kotlinx.coroutines.launch
import pt.pak3nuh.hollywood.sandbox.Loggers
import pt.pak3nuh.hollywood.sandbox.actor.VetFactory
import pt.pak3nuh.hollywood.sandbox.actor.getPetClinic
import pt.pak3nuh.hollywood.sandbox.owner.ContactService
import pt.pak3nuh.hollywood.sandbox.owner.CreditCard
import pt.pak3nuh.hollywood.sandbox.owner.OwnerContacts
import pt.pak3nuh.hollywood.sandbox.pet.Pet
import pt.pak3nuh.hollywood.sandbox.pet.PetId
import pt.pak3nuh.hollywood.sandbox.vet.Vet
import pt.pak3nuh.hollywood.system.ActorSystem

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
