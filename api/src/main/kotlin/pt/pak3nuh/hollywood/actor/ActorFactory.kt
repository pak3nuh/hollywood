package pt.pak3nuh.hollywood.actor

import pt.pak3nuh.hollywood.actor.proxy.ActorProxy
import pt.pak3nuh.hollywood.actor.proxy.ProxyConfiguration
import pt.pak3nuh.hollywood.system.ActorManager
import kotlin.reflect.KClass

/**
 * An actor factory should create instances of user defined [T] implementations.
 * This interface just contains the bare minimum to provide the system enough metadata to _glue_ things together.
 *
 * Since specialized interfaces for each actor will be provided, it isn't expected that the user implements
 * this interface directly, except for custom proxies.
 */
interface ActorFactory<T : Any, P : ActorProxy<T>> {
    /**
     * Creates a proxy suitable for actors of type [T].
     */
    fun createProxy(delegate: T, config: ProxyConfiguration): P

    /**
     * The class of the actors that this factory will provide.
     *
     * Any object that subclasses of this type produced on [ActorManager.createActor] that fails to evaluate
     * [KClass.isInstance] will throw [IllegalStateException].
     */
    val actorKClass: KClass<T>
    /**
     * The class of the proxy for the actor [T].
     */
    val proxyKClass: KClass<P>
}
