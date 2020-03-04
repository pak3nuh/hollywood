package pt.pakenuh.hollywood.sandbox.actor

import pt.pak3nuh.hollywood.processor.Actor
import pt.pakenuh.hollywood.sandbox.Loggers
import pt.pakenuh.hollywood.sandbox.pet.Pet

@Actor
interface PetActor {
    suspend fun applyTreatment(treatment: Treatment)
}

class PetFactory(private val actors: ClinicActors) : FactoryBase<PetActor, PetActorProxy>(PetActor::class, PetActorProxy::class, ::PetActorProxy) {
    fun createPet(pet: Pet): PetActor = PetActorImpl(pet, actors)
}

private class PetActorImpl(private val pet: Pet, private val actors: ClinicActors) : PetActor {

    private val logger = Loggers.getLogger<PetActorImpl>()
    private val petName = pet.petId.name

    override suspend fun applyTreatment(treatment: Treatment) {
        logger.info("Applying treatment $petName")
        pet.applyTreatment(treatment)
        logger.fine("$petName is ready")
        actors.getClinic().petReady(pet)
    }
}
