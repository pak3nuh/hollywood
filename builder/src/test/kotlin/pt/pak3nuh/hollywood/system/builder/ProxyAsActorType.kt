package pt.pak3nuh.hollywood.system.builder

import pt.pak3nuh.hollywood.actor.ActorFactory
import pt.pak3nuh.hollywood.actor.proxy.ProxyConfiguration
import kotlin.reflect.KClass

interface Person

class PersonProxy(override val delegate: PersonProxy) : BaseProxy<PersonProxy>(), Person by delegate

class PersonFactory : ActorFactory<PersonProxy, PersonProxy> {
    override fun createProxy(delegate: PersonProxy, config: ProxyConfiguration): PersonProxy = PersonProxy(delegate)
    override val actorKClass: KClass<PersonProxy> = PersonProxy::class
    override val proxyKClass: KClass<PersonProxy> = PersonProxy::class
}
