package pt.pak3nuh.hollywood.system

import pt.pak3nuh.hollywood.actor.ActorFactory
import pt.pak3nuh.hollywood.actor.proxy.ActorProxy
import pt.pak3nuh.hollywood.actor.proxy.ProxyConfiguration
import kotlin.reflect.KClass

interface Greeter {
    suspend fun sayHello(): String
}

class GreeterActor : Greeter {
    override suspend fun sayHello(): String = "Hello world"
}

class GreeterProxy(override val delegate: Greeter, override val actorId: String) : ActorProxy<Greeter>, Greeter by delegate

class GreeterFactory : ActorFactory<Greeter, GreeterProxy> {
    var createdActors = 0
    var createdProxies = 0
    override fun createProxy(delegate: Greeter, config: ProxyConfiguration): GreeterProxy {
        createdProxies++
        return GreeterProxy(delegate, config.actorId)
    }

    override val actorKClass: KClass<Greeter> = Greeter::class
    override val proxyKClass: KClass<GreeterProxy> = GreeterProxy::class
    fun createGreeter(): GreeterActor {
        createdActors++
        return GreeterActor()
    }
}
