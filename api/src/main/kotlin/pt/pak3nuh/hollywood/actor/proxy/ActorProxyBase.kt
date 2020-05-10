package pt.pak3nuh.hollywood.actor.proxy

/**
 * Base class for generated actor proxies.
 *
 * Any custom proxy that is expected to be plugged in into the code generator, should expose the same public interface.
 * Consider extending this class for increased compatibility with the code generator.
 */
open class ActorProxyBase<T>(override val delegate: T, private val configuration: ProxyConfiguration) : ActorProxy<T> {
    final override val actorId: String
        get() = configuration.actorId

    protected suspend fun <T> execCall(delegateCall: suspend () -> T): T {
        return delegateCall()
    }
}
