package pt.pak3nuh.hollywood.system

import pt.pak3nuh.hollywood.actor.proxy.ActorScope

class SystemImpl(
        override val actorManager: ActorManagerImpl
) : ActorSystem {

    override val actorScope: ActorScope
        get() = actorManager.actorScope

    override suspend fun shutdown() {
        // TODO dispose all actors
    }
}
