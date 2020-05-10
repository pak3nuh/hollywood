package pt.pakenuh.hollywood.sandbox.actor

import pt.pak3nuh.hollywood.system.ActorManager
import pt.pakenuh.hollywood.sandbox.owner.OwnerId
import pt.pakenuh.hollywood.sandbox.pet.Pet
import pt.pakenuh.hollywood.sandbox.vet.Vet

class ClinicActors(private val actorManager: ActorManager) {

    fun getClinic(): ClinicActor {
        return actorManager.getOrCreateActor(ClinicActor.CLINIC_ID, ClinicBinaryFactory::class) { it.createClinic() }
    }

    fun getVet(vet: Vet): VetActor {
        return actorManager.getOrCreateActor(vet.name, VetFactory::class) { it.createVet() }
    }

    fun getOwner(ownerId: OwnerId): OwnerActor {
        return actorManager.getOrCreateActor(ownerId.id, OwnerFactory::class) {
            it.createOwner(ownerId)
        }
    }

    fun getPet(pet: Pet): PetActor {
        return actorManager.getOrCreateActor(pet.petId.registryId, PetFactory::class) {
            it.createPet(pet)
        }
    }
}
