package pt.pak3nuh.hollywood.system

/**
 * The main entry point for the actor system.
 */
interface ActorSystem {
    /**
     * Manager to create or dispose actors
     */
    val actorManager: ActorManager

    /**
     * Closes all resources and shuts down the system.
     */
    suspend fun shutdown()
}
