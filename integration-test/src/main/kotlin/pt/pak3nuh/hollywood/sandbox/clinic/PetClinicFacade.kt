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

class PetClinicFacade(
        actorSystem: ActorSystem,
        private val contactService: ContactService
) {

    private val petClinicActor = actorSystem.getPetClinic()
    private val logger = Loggers.getLogger<PetClinicFacade>()
    val actorScope = actorSystem.actorScope

    suspend fun registerVet(vet: Vet) {
        petClinicActor.registerVet(vet)
    }

    suspend fun checkinPet(pet: Pet, contacts: OwnerContacts) {
        contactService.registerContact(pet.petId.ownerId, contacts)
        petClinicActor.checkinPet(pet)
    }

    suspend fun checkoutPet(petId: PetId, creditCard: CreditCard): Receipt {
        return petClinicActor.checkoutPet(petId, creditCard)
    }

    suspend fun seePet(petId: PetId, actions: (Pet) -> Unit) {
        val pet = petClinicActor.getPetToSee(petId)
        actions(pet)
    }

    suspend fun currentPets(): List<PetId> {
        return petClinicActor.getPets()
    }

    suspend fun waitClosing() {
        petClinicActor.waitClosing()
    }
}
