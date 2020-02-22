package pt.pakenuh.hollywood.sandbox.actor

import pt.pak3nuh.hollywood.system.ActorManager
import pt.pakenuh.hollywood.sandbox.owner.OwnerId
import pt.pakenuh.hollywood.sandbox.pet.Pet
import pt.pakenuh.hollywood.sandbox.vet.Vet

class ClinicActors(private val actorManager: ActorManager) {

    fun getClinic(): ClinicActor {
        return actorManager.getOrCreateActor(ClinicActor.CLINIC_ID, ClinicFactory::class) { it.createClinic() }
    }

    fun getVet(vet: Vet): VetActor {
        return actorManager.getOrCreateActor("VET-vet.name", VetFactory::class) { it.createVet(vet) }
    }

    suspend fun getOwner(ownerId: OwnerId): OwnerActor {
        val ownerContacts = getClinic().getOwnerContact(ownerId)
        return actorManager.getOrCreateActor("OWNER-${ownerId.id}", OwnerFactory::class) {
            it.createOwner(ownerContacts)
        }
    }

    fun getPet(pet: Pet): PetActor {
        return actorManager.getOrCreateActor("PET-${pet.petId.registryId}", PetFactory::class) {
            it.createPet(pet)
        }
    }
}
