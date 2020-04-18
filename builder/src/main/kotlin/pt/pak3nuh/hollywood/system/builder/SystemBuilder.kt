package pt.pak3nuh.hollywood.system.builder

import pt.pak3nuh.hollywood.actor.ActorFactory
import pt.pak3nuh.hollywood.actor.proxy.ActorProxy
import pt.pak3nuh.hollywood.system.ActorManagerImpl
import pt.pak3nuh.hollywood.system.ActorSystem
import pt.pak3nuh.hollywood.system.AnyActorFactory
import pt.pak3nuh.hollywood.system.FactoryRepositoryImpl
import pt.pak3nuh.hollywood.system.SystemImpl
import pt.pak3nuh.hollywood.system.actor.message.serializer.Deserializer
import pt.pak3nuh.hollywood.system.actor.message.serializer.Serializer
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

private typealias DeferredBuilder<T> = (ActorSystem) -> T
private typealias FactoryBuilder<F> = (ActorSystem, SystemBuilder.PropertyGetter) -> F

/**
 * Entry point to build [ActorSystem] instances.
 *
 * It receives functions to build all the resources needed. The [ActorSystem] instance in these functions isn't fully
 * built, it's purpose is to solve any cyclic dependencies for resources that need an instance of [ActorSystem] to work.
 */
class SystemBuilder {

    private val factoryBuilderMap = mutableMapOf<KClass<out AnyActorFactory>, FactoryBuilder<AnyActorFactory>>()
    private val buildProperties = mutableMapOf<Property<out Any>, DeferredBuilder<Any>>()

    /**
     * Registers a factory to be available in the actor system.
     */
    fun <T : Any, P : ActorProxy<T>, F : ActorFactory<T, P>> registerFactory(kClass: KClass<F>, builder: FactoryBuilder<F>): SystemBuilder {
        // Cannot add P : T because Java forbids it and Kotlin abides
        // https://stackoverflow.com/questions/43790137/why-cant-type-parameter-in-kotlin-have-any-other-bounds-if-its-bounded-by-anot
        factoryBuilderMap[kClass] = builder
        return this
    }

    /**
     * Binds a property key to a value that can be used **during** the build process of [ActorSystem] through [PropertyGetter].
     *
     * These properties are built before everything else to provide dependencies for other components.
     * @param property The property key
     * @param valueBuilder The build function for the property.
     */
    fun <T : Any> withProperty(property: Property<T>, valueBuilder: DeferredBuilder<T>): SystemBuilder {
        buildProperties[property] = valueBuilder
        return this
    }

    /**
     * Does final integrity checks and builds the system.
     * @throws IllegalStateException If no factories were provided.
     * @throws IllegalStateException If the actor factory has some invalid configuration.
     */
    fun build(): ActorSystem {
        check(factoryBuilderMap.isNotEmpty()) { "There must be at least one actor factory" }

        val factoryMap = mutableMapOf<KClass<out AnyActorFactory>, AnyActorFactory>()
        val actorSystem = SystemImpl(ActorManagerImpl(FactoryRepositoryImpl(factoryMap), Serializer(), Deserializer()))
        var systemBuilt = false

        val buildProperties = buildProperties.mapValues { it.value(actorSystem) }
        val propGetter = object : PropertyGetter {
            override operator fun <T : Any> get(property: Property<T>): T {
                check(!systemBuilt) { "Properties inaccessible, system has been built." }
                @Suppress("UNCHECKED_CAST")
                return buildProperties.getValue(property) as T
            }
        }

        factoryBuilderMap.forEach { (factoryInterface, builder) ->
            val factory = builder(actorSystem, propGetter)
            check(!factory.actorKClass.isSubclassOf(ActorProxy::class)) { "Actor type can't be a proxy" }
            check(factory.proxyKClass.isSubclassOf(factory.actorKClass)) { "Proxy must be a subclass of the actor" }
            factoryMap[factoryInterface] = factory
        }

        systemBuilt = true
        return actorSystem
    }

    /**
     * A typed instance of a property. Users should specialize through **object** instances like
     * ```
     * object MyProperty<String>: Property<String>()
     * ```
     * Can be later retrieved by [PropertyGetter.get].
     */
    abstract class Property<T>

    /**
     * Obtains instances of properties defined on [SystemBuilder.withProperty].
     */
    interface PropertyGetter {
        /**
         * Gets property previously set.
         * @throws IllegalStateException If a property is accessed after the system was built.
         * @throws NoSuchElementException If a property doesn't exist.
         */
        operator fun <T : Any> get(property: Property<T>): T
    }
}

