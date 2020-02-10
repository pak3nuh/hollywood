package pt.pakenuh.hollywood.sandbox

import pt.pak3nuh.hollywood.system.ActorSystem
import pt.pakenuh.hollywood.sandbox.actor.getPetClinic
import pt.pakenuh.hollywood.sandbox.clinic.ClinicFacade
import pt.pakenuh.hollywood.sandbox.owner.OwnerFacade
import pt.pakenuh.hollywood.sandbox.pet.Pet

class PetClinicImpl(private val actorSystem: ActorSystem) : PetClinic {
    override val ownerFacade: OwnerFacade = OwnerFacadeImpl()
    override val clinicFacade: ClinicFacade = ClinicFacadeImpl()

    private inner class OwnerFacadeImpl : OwnerFacade {
        override suspend fun checkinPet(pet: Pet) {
            actorSystem.getPetClinic().checkinPet(pet)
        }
    }

    private inner class ClinicFacadeImpl : ClinicFacade
}
