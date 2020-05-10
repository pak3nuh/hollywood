package pt.pak3nuh.hollywood.actor.proxy

import pt.pak3nuh.hollywood.actor.message.MessageBuilder
import pt.pak3nuh.hollywood.actor.message.serializer.Deserializer
import pt.pak3nuh.hollywood.actor.message.serializer.Serializer
import pt.pak3nuh.hollywood.processor.Actor

/**
 * Base interface that must be present in all the proxies.
 */
interface ActorProxy<T> {
    /**
     * The actual user defined actor.
     *
     * Because an [ActorProxy] of [T] is also a [T], it is possible to just return **this**, although highly discouraged,
     * because it goes against the expected structure.
     * @see Actor
     */
    val delegate: T

    /**
     * A unique ID for each actor.
     */
    val actorId: String
}

/**
 * Contains additional data for proxy behaviour.
 */
interface ProxyConfiguration {
    val actorId: String
    val serializer: Serializer
    val deserializer: Deserializer
    fun newMessageBuilder(): MessageBuilder
}
