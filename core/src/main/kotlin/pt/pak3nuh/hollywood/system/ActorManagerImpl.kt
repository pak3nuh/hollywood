package pt.pak3nuh.hollywood.system

import pt.pak3nuh.hollywood.actor.ActorFactory
import pt.pak3nuh.hollywood.actor.message.MessageBuilder
import pt.pak3nuh.hollywood.actor.message.serializer.Serializer
import pt.pak3nuh.hollywood.actor.proxy.ActorProxy
import pt.pak3nuh.hollywood.actor.proxy.ProxyConfiguration
import pt.pak3nuh.hollywood.system.actor.message.createMessageBuilder
import java.lang.ref.ReferenceQueue
import java.lang.ref.WeakReference
import java.util.UUID
import java.util.concurrent.ConcurrentSkipListMap
import kotlin.reflect.KClass
import kotlin.reflect.full.cast

class ActorManagerImpl(
        private val factoryRepository: FactoryRepository,
        private val serializer: Serializer
) : ActorManager {

    private val referenceQueue = ReferenceQueue<ActorProxy<*>>()
    // not using the InternalActorId because inline classes are boxed on generics
    private val managedActors = ConcurrentSkipListMap<String, AtomicHolder>()

    override fun <T : Any, P : ActorProxy<T>, F : ActorFactory<T, P>> createActor(factoryClass: KClass<out F>, creator: (F) -> T): T {
        return getOrCreateActor(UUID.randomUUID().toString(), factoryClass, creator)
    }

    override fun <T : Any, P : ActorProxy<T>, F : ActorFactory<T, P>> getOrCreateActor(actorId: String, factoryClass: KClass<out F>, creator: (F) -> T): T {
        purgeReferences()
        // get reified factory
        val factory = factoryRepository[factoryClass]
        val internalActorId = InternalActorId.fromExternal(actorId, factory.actorKClass)
        val holder: AtomicHolder = managedActors.computeIfAbsent(internalActorId.fullActorId) {
            AtomicHolder(referenceQueue) {
                // creates actor
                val actorInstance = creator(factory)
                check(factory.actorKClass.isInstance(actorInstance)) {
                    "Actor instance provided is not of the type ${factory.actorKClass}"
                }
                check(!factory.proxyKClass.isInstance(actorInstance)) { "Actor created can't be a proxy" }
                // creates proxy
                val proxy = factory.createProxy(actorInstance, Configuration(internalActorId, serializer))
                check(actorInstance::class != proxy::class) { "Actor and proxy have the same type" }
                proxy
            }
        }

        val proxy: ActorProxy<*>? = holder.reference.get()
        return if (proxy != null) {
            // actor still live in memory
            factory.actorKClass.cast(proxy)
        } else {
            // actor was not in the reference queue but was collected
            // forces enqueue because weak references aren't enqueued immediately
            holder.reference.enqueue()
            getOrCreateActor(actorId, factoryClass, creator)
        }
    }


    internal fun <T : Any> getActor(actorId: String, actorKClass: KClass<T>): T? {
        val internalActorId = InternalActorId.fromExternal(actorId, actorKClass)
        val actorProxy = managedActors[internalActorId.fullActorId]?.reference?.get()
        // no need to check for instance type because the class name was encoded in the key
        return actorProxy?.let { actorKClass.cast(it) }
    }

    /**
     * Removes all references that were enqueued and collected
     */
    private fun purgeReferences() {
        while (true) {
            when (val reference = referenceQueue.poll() as ProxyReference?) {
                null -> return
                else -> removeAndCleanup(reference.actorId)
            }
        }
    }

    override fun disposeActor(actor: Any) {
        require(actor is ActorProxy<*>) { "Object is not a valid proxy" }
        removeAndCleanup(actor.actorId)
    }

    private fun removeAndCleanup(actorId: String) {
        managedActors.remove(actorId)
    }

    /**
     * This class is required to guarantee proxy creation only occurs once because
     * [ConcurrentSkipListMap.computeIfAbsent] function is not atomic.
     */
    private class AtomicHolder(queue: ReferenceQueue<in ActorProxy<*>>, proxyFunction: () -> ActorProxy<*>) {
        val reference: ProxyReference by lazy {
            ProxyReference(proxyFunction(), queue)
        }
    }

    private class ProxyReference(referent: ActorProxy<*>, queue: ReferenceQueue<in ActorProxy<*>>) : WeakReference<ActorProxy<*>>(referent, queue) {
        val actorId = referent.actorId
    }

}

private class Configuration(private val internalActorId: InternalActorId, override val serializer: Serializer) : ProxyConfiguration {
    override val actorId: String
        get() = internalActorId.fullActorId

    override fun newMessageBuilder(): MessageBuilder = createMessageBuilder()
}

private inline class InternalActorId(val fullActorId: String) {
    companion object {
        /**
         * For uniqueness, external actor ids must be concatenated with their actor interface
         */
        fun fromExternal(actorId: String, actorKClass: KClass<*>): InternalActorId {
            return InternalActorId("${actorKClass.qualifiedName}:$actorId")
        }
    }
}
