package pt.pak3nuh.hollywood.system

import pt.pak3nuh.hollywood.actor.proxy.ActorScope

/**
 * The main entry point for the actor system.
 */
interface ActorSystem {
    /**
     * Manager to create or dispose actors
     */
    val actorManager: ActorManager

    /**
     * Coroutine scope in which the actors run
     */
    val actorScope: ActorScope

    /**
     * Closes all resources and shuts down the system.
     */
    suspend fun shutdown()
}
