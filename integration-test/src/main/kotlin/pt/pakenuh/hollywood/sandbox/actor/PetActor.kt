package pt.pakenuh.hollywood.sandbox.actor

import pt.pak3nuh.hollywood.processor.Actor
import pt.pakenuh.hollywood.sandbox.pet.Pet

@Actor
interface PetActor {
    suspend fun getPet(): Pet
}
