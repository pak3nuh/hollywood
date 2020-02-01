package pt.pak3nuh.hollywood.system

class SystemImpl(
        override val actorManager: ActorManager
) : ActorSystem {

    override suspend fun shutdown() {
        // TODO dispose all actors
    }
}
