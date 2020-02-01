package pt.pak3nuh.hollywood.system

import pt.pak3nuh.hollywood.actor.ActorFactory
import pt.pak3nuh.hollywood.actor.proxy.ActorProxy
import pt.pak3nuh.hollywood.actor.proxy.ProxyConfiguration
import java.util.UUID
import java.util.concurrent.ConcurrentSkipListMap
import kotlin.reflect.KClass
import kotlin.reflect.full.cast

class ActorManagerImpl(private val factoryRepository: FactoryRepository) : ActorManager {

    private val managedActors = ConcurrentSkipListMap<String, ProxyHolder>()

    override fun <T : Any, P : ActorProxy<T>, F : ActorFactory<T, P>> createActor(factoryClass: KClass<out F>, creator: (F) -> T): T {
        return getOrCreateActor(UUID.randomUUID().toString(), factoryClass, creator)
    }

    override fun <T : Any, P : ActorProxy<T>, F : ActorFactory<T, P>> getOrCreateActor(actorId: String, factoryClass: KClass<out F>, creator: (F) -> T): T {
        // get reified factory
        val factory = factoryRepository[factoryClass]
        val holder = managedActors.computeIfAbsent(actorId) {
            ProxyHolder {
                // creates actor
                val actorInstance = creator(factory)
                check(factory.actorKClass.isInstance(actorInstance)) {
                    "Actor instance provided is not of the type ${factory.actorKClass}"
                }
                check(!factory.proxyKClass.isInstance(actorInstance)) { "Actor created can't be a proxy" }
                // creates proxy
                val proxy = factory.createProxy(actorInstance, Configuration(actorId))
                check(actorInstance::class != proxy::class) { "Actor and proxy have the same type" }
                proxy
            }
        }
        return factory.actorKClass.cast(holder.proxy)
    }

    override fun disposeActor(actor: Any) {
        require(actor is ActorProxy<*>) { "Object is not a valid proxy" }
        managedActors.remove(actor.proxyId)?.proxy?.dispose()
    }

    /**
     * This class is required to guarantee proxy creation only occurs once because
     * [ConcurrentSkipListMap.computeIfAbsent] function is not atomic.
     */
    private class ProxyHolder(proxyFunction: () -> ActorProxy<*>) {
        val proxy: ActorProxy<*> by lazy(proxyFunction)
    }

    private class Configuration(override val proxyId: String) : ProxyConfiguration
}
