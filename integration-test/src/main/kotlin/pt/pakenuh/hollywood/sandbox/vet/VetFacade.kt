package pt.pakenuh.hollywood.sandbox.vet

import pt.pakenuh.hollywood.sandbox.clinic.PetStatus
import pt.pakenuh.hollywood.sandbox.clinic.PetUpdateAction
import pt.pakenuh.hollywood.sandbox.pet.Pet
import pt.pakenuh.hollywood.sandbox.pet.PetId

interface VetFacade {
    suspend fun analysePet(pet: Pet, checkType: CheckType): AnalysisReport
    suspend fun contactOwner(petId: PetId, petStatus: PetStatus): PetUpdateAction
    suspend fun applyTreatment(pet: Pet, treatment: Treatment): PetStatus
}
