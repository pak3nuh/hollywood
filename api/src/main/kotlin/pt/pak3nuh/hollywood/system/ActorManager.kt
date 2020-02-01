package pt.pak3nuh.hollywood.system

import pt.pak3nuh.hollywood.actor.ActorFactory
import pt.pak3nuh.hollywood.actor.proxy.ActorProxy
import kotlin.reflect.KClass

/**
 * Manages actor lifecycle.
 */
interface ActorManager {
    /**
     * Obtains a new actor instance using the [factoryClass] instance with the [creator] function.
     * @throws NoSuchElementException If the class was not registered.
     * @throws IllegalStateException If the actor created by the [creator] fails integrity checks.
     */
    fun <T : Any, P : ActorProxy<T>, F : ActorFactory<T, P>> createActor(factoryClass: KClass<out F>, creator: (F) -> T): T

    /**
     * Gets an actor by its id or creates a new one like [ActorManager.createActor].
     *
     * This is an atomic operation.
     * @throws NoSuchElementException If the class was not registered.
     * @throws IllegalStateException If the actor created by the [creator] fails integrity checks.
     */
    fun <T : Any, P : ActorProxy<T>, F : ActorFactory<T, P>> getOrCreateActor(
            actorId: String,
            factoryClass: KClass<out F>,
            creator: (F) -> T
    ): T

    /**
     * Terminates an actor and the resources it holds.

     * This method can be called multiple times for the same object.
     *
     * @param actor An actor created by this manager.
     * @throws IllegalArgumentException If the [actor] is invalid.
     */
    fun disposeActor(actor: Any)
}
