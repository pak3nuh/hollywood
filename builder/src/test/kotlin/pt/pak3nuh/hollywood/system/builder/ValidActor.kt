package pt.pak3nuh.hollywood.system.builder

import pt.pak3nuh.hollywood.actor.ActorFactory
import pt.pak3nuh.hollywood.actor.proxy.ActorProxy
import pt.pak3nuh.hollywood.actor.proxy.ProxyConfiguration
import kotlin.reflect.KClass

interface Greeter {
    fun sayHello(): String
}

class GreeterImpl(private val greet: String) : Greeter {
    override fun sayHello() = greet
}

abstract class BaseProxy<T> : ActorProxy<T> {
    override val actorId: String = "some id"
}

class GreeterProxy(override val delegate: Greeter) : BaseProxy<Greeter>(), Greeter by delegate

class GreeterFactory(private val greet: String? = null) : ActorFactory<Greeter, GreeterProxy> {
    override fun createProxy(delegate: Greeter, config: ProxyConfiguration): GreeterProxy = GreeterProxy(delegate)
    override val actorKClass: KClass<Greeter> = Greeter::class
    override val proxyKClass: KClass<GreeterProxy> = GreeterProxy::class
    fun createActor() = GreeterImpl(greet ?: "Hello World!")
}
