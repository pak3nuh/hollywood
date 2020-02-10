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
        return actorManager.getOrCreateActor(vet.name, VetFactory::class) { it.createVet(vet) }
    }

    fun getOwner(ownerId: OwnerId): OwnerActor {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getPet(pet: Pet): PetActor {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
