package pt.pak3nuh.hollywood.sandbox.actor

import pt.pak3nuh.hollywood.processor.Actor
import pt.pak3nuh.hollywood.sandbox.Loggers
import pt.pak3nuh.hollywood.sandbox.pet.Pet

@Actor
interface PetActor {
    suspend fun applyTreatment(treatment: Treatment)
}

class PetFactory(private val actors: ClinicActors) : PetActorBaseFactory {
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
