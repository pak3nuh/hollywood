package pt.pakenuh.hollywood.sandbox.actor

import pt.pak3nuh.hollywood.actor.ActorFactory
import pt.pak3nuh.hollywood.actor.proxy.ActorProxy
import pt.pak3nuh.hollywood.actor.proxy.ProxyConfiguration
import kotlin.reflect.KClass

abstract class FactoryBase<A : Any, P : ActorProxy<A>>(
        final override val actorKClass: KClass<A>,
        final override val proxyKClass: KClass<P>,
        private val creator: (A, String) -> P
) : ActorFactory<A, P> {
    final override fun createProxy(delegate: A, config: ProxyConfiguration): P = creator(delegate, config.actorId)
}
