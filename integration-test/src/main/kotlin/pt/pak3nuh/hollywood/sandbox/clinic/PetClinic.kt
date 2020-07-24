package pt.pak3nuh.hollywood.sandbox.clinic

import pt.pak3nuh.hollywood.actor.proxy.ActorScope
import pt.pak3nuh.hollywood.sandbox.owner.CreditCard
import pt.pak3nuh.hollywood.sandbox.owner.OwnerContacts
import pt.pak3nuh.hollywood.sandbox.pet.Pet
import pt.pak3nuh.hollywood.sandbox.pet.PetId

interface PetClinic {
    suspend fun checkinPet(pet: Pet, contacts: OwnerContacts)
    suspend fun checkoutPet(petId: PetId, creditCard: CreditCard): Receipt
    suspend fun seePet(petId: PetId, actions: (Pet) -> Unit)
    suspend fun currentPets(): List<PetId>
    suspend fun waitClosing()
    val actorScope: ActorScope
}
