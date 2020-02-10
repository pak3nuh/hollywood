package pt.pakenuh.hollywood.sandbox.actor

import pt.pak3nuh.hollywood.actor.proxy.ActorProxy
import pt.pak3nuh.hollywood.processor.Actor
import pt.pakenuh.hollywood.sandbox.pet.Pet

@Actor
interface PetActor {
    suspend fun applyTreatment(treatment: Treatment)
}

class PetProxy(override val delegate: PetActor, override val actorId: String) : ActorProxy<PetActor>, PetActor by delegate

class PetFactory(private val actors: ClinicActors) : FactoryBase<PetActor, PetProxy>(PetActor::class, PetProxy::class, ::PetProxy) {
    fun createPet(pet: Pet): PetActor = PetActorImpl(pet, actors)
}

private class PetActorImpl(private val pet: Pet, private val actors: ClinicActors) : PetActor {
    override suspend fun applyTreatment(treatment: Treatment) {
        pet.applyTreatment(treatment)
        actors.getClinic().petReady(pet)
    }
}
