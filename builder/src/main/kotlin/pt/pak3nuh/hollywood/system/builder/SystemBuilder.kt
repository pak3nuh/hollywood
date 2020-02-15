package pt.pak3nuh.hollywood.system.builder

import pt.pak3nuh.hollywood.actor.ActorFactory
import pt.pak3nuh.hollywood.actor.proxy.ActorProxy
import pt.pak3nuh.hollywood.system.ActorManagerImpl
import pt.pak3nuh.hollywood.system.ActorSystem
import pt.pak3nuh.hollywood.system.AnyActorFactory
import pt.pak3nuh.hollywood.system.FactoryRepositoryImpl
import pt.pak3nuh.hollywood.system.SystemImpl
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

class SystemBuilder {

    private val factoryMap = mutableMapOf<KClass<out AnyActorFactory>, AnyActorFactory>()

    /**
     * Registers a factory to be available in the actor system.
     * @throws IllegalArgumentException If the actor factory has some invalid configuration.
     */
    fun <T : Any, P : ActorProxy<T>, F : ActorFactory<T, P>> registerFactory(kClass: KClass<F>, instance: F): SystemBuilder {
        // Cannot add P : T because Java forbids it and Kotlin abides
        // https://stackoverflow.com/questions/43790137/why-cant-type-parameter-in-kotlin-have-any-other-bounds-if-its-bounded-by-anot
        require(!instance.actorKClass.isSubclassOf(ActorProxy::class)) { "Actor type can't be a proxy" }
        require(instance.proxyKClass.isSubclassOf(instance.actorKClass)) { "Proxy must be a subclass of the actor" }
        factoryMap[kClass] = instance
        return this
    }

    /**
     * Does final integrity checks and builds the system.
     * @throws IllegalStateException If no factories were provided.
     */
    fun build(): ActorSystem {
        check(factoryMap.isNotEmpty()) { "There must be at least one actor factory" }
        return SystemImpl(ActorManagerImpl(FactoryRepositoryImpl(factoryMap)))
    }
}

