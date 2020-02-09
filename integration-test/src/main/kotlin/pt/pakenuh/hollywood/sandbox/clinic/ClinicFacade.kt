package pt.pakenuh.hollywood.sandbox.clinic

import pt.pakenuh.hollywood.sandbox.pet.PetId
import pt.pakenuh.hollywood.sandbox.vet.Vet

interface ClinicFacade {
    suspend fun addVet(vet: Vet)
    suspend fun currentPets(): List<PetId>
}
