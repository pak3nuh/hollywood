package pt.pakenuh.hollywood.sandbox.actor

import pt.pak3nuh.hollywood.actor.proxy.ActorProxy
import pt.pak3nuh.hollywood.processor.Actor
import pt.pak3nuh.hollywood.system.ActorSystem
import pt.pakenuh.hollywood.sandbox.PetClinicException
import pt.pakenuh.hollywood.sandbox.pet.Pet
import pt.pakenuh.hollywood.sandbox.vet.Vet

@Actor
interface ClinicActor {
    suspend fun checkinPet(pet: Pet)
    suspend fun petReady(pet: Pet)

    companion object {
        const val CLINIC_ID = "clinic unique id"
    }
}

class ClinicProxy(override val delegate: ClinicActor, override val actorId: String) : ActorProxy<ClinicActor>, ClinicActor by delegate

class ClinicFactory(private val vets: List<Vet>) : FactoryBase<ClinicActor, ClinicProxy>(ClinicActor::class, ClinicProxy::class, ::ClinicProxy) {
    fun createClinic(): ClinicActor = ClinicActorImpl(vets)
}

internal class ClinicActorImpl(private val vets: List<Vet>, private val clinicActors: ClinicActors) : ClinicActor {

    private val pets = mutableMapOf<String, Pet>()

    override suspend fun checkinPet(pet: Pet) {
        vets.forEach {
            if (clinicActors.getVet(it).trySchedule(pet)) {
                pets[pet.petId.registryId] = pet
                return
            }
        }
        throw PetClinicException("No vets available")
    }

}

internal fun ActorSystem.getPetClinic(): ClinicActor =
        actorManager.getOrCreateActor(ClinicActor.CLINIC_ID, ClinicFactory::class, ClinicFactory::createClinic)
