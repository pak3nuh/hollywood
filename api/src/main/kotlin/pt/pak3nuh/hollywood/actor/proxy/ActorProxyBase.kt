package pt.pak3nuh.hollywood.actor.proxy

open class ActorProxyBase<T>(final override val delegate: T, private val configuration: ProxyConfiguration) : ActorProxy<T> {
    final override val actorId: String
        get() = configuration.actorId
}
