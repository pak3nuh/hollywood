package pt.pak3nuh.hollywood.system

import pt.pak3nuh.hollywood.actor.ActorFactory
import pt.pak3nuh.hollywood.actor.proxy.ActorProxy
import kotlin.reflect.KClass
import kotlin.reflect.full.cast

typealias AnyActorFactory = ActorFactory<out Any, out ActorProxy<out Any>>

/**
 * Contains all the registered factories when building the actor system.
 */
interface FactoryRepository {
    /**
     * Gets a factory of type [factoryClass].
     * @throws NoSuchElementException If the class was not registered.
     */
    operator fun <T : Any, P : ActorProxy<T>, F : ActorFactory<T, P>> get(factoryClass: KClass<out F>): F
}

class FactoryRepositoryImpl(private val factoryMap: Map<KClass<out AnyActorFactory>, AnyActorFactory>)
    : FactoryRepository {

    override fun <T : Any, P : ActorProxy<T>, F : ActorFactory<T, P>> get(factoryClass: KClass<out F>): F {
        // get reified factory
        val factory = factoryMap[factoryClass]
                ?: throw NoSuchElementException("Factory for class $factoryClass was not registered")
        return factoryClass.cast(factory)
    }

}
