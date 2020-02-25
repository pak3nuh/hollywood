package pt.pakenuh.hollywood.sandbox.clinic

import pt.pakenuh.hollywood.sandbox.pet.PetId

data class Receipt(val petId: PetId, val total: Int)
