package pt.pakenuh.hollywood.sandbox.actor

import kotlinx.coroutines.delay
import pt.pak3nuh.hollywood.actor.proxy.ActorProxy
import pt.pak3nuh.hollywood.processor.Actor
import pt.pakenuh.hollywood.sandbox.pet.Pet
import pt.pakenuh.hollywood.sandbox.vet.Vet
import java.util.LinkedList
import java.util.Queue

@Actor
interface VetActor {
    suspend fun trySchedule(pet: Pet): Boolean
    suspend fun startWork(): Nothing
}

class VetProxy(override val delegate: VetActor, override val actorId: String) : ActorProxy<VetActor>, VetActor by delegate

class VetFactory : FactoryBase<VetActor, VetProxy>(VetActor::class, VetProxy::class, ::VetProxy) {
    fun createVet(vet: Vet): VetActor = VetActorImpl(vet)
}

class VetActorImpl(private val vet: Vet, private val actors: ClinicActors) : VetActor {

    private val slots: Queue<Pet> = LinkedList()

    override suspend fun trySchedule(pet: Pet): Boolean {
        return if (slots.size == MAX_SCHEDULE) {
            false
        } else {
            slots.offer(pet)
        }
    }

    override suspend fun startWork(): Nothing {
        while (true) {
            val pet: Pet? = slots.poll()
            if (pet == null) {
                delay(DELAY_TIME_MS)
            } else {
                checkPet(pet)
            }
        }
    }

    private suspend fun checkPet(pet: Pet) {
        if (isHealthy(pet)) {
            actors.getClinic().petReady(pet)
        } else {
            val petActor = actors.getPet(pet)
            // todo check what to do and preform treatment
        }
    }

    private fun isHealthy(pet: Pet): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private companion object {
        private const val MAX_SCHEDULE = 5
        private const val DELAY_TIME_MS = 1_000L
    }
}
