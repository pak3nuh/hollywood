package pt.pakenuh.hollywood.sandbox.clinic

import pt.pakenuh.hollywood.sandbox.actor.OwnerContacts
import pt.pakenuh.hollywood.sandbox.owner.CreditCard
import pt.pakenuh.hollywood.sandbox.pet.Pet
import pt.pakenuh.hollywood.sandbox.pet.PetId

interface PetClinic {
    suspend fun checkinPet(pet: Pet, contacts: OwnerContacts)
    suspend fun checkoutPet(petId: PetId, creditCard: CreditCard): Receipt
    suspend fun seePet(petId: PetId, actions: (Pet) -> Unit)
    suspend fun currentPets(): List<PetId>
    suspend fun waitClosing()
}
