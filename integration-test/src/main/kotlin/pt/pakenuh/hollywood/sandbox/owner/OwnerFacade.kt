package pt.pakenuh.hollywood.sandbox.owner

import pt.pakenuh.hollywood.sandbox.clinic.PetStatus
import pt.pakenuh.hollywood.sandbox.clinic.PetUpdateAction
import pt.pakenuh.hollywood.sandbox.clinic.Receipt
import pt.pakenuh.hollywood.sandbox.pet.Pet
import pt.pakenuh.hollywood.sandbox.pet.PetId

interface OwnerFacade {
    suspend fun checkinPet(pet: Pet)
    suspend fun checkoutPet(petId: PetId, creditCard: CreditCard): Receipt
    suspend fun registerForUpdates(petUpdate: (PetStatus) -> PetUpdateAction)
    suspend fun seePet(petId: PetId, actions: (Pet) -> Unit)
}
