package pt.pak3nuh.hollywood.actor.message.serializer

import pt.pak3nuh.hollywood.actor.message.Message
import pt.pak3nuh.hollywood.actor.message.Response

/**
 * Serializer for communication items.
 * @see Deserializer
 */
interface Serializer {
    fun serialize(message: Message): ByteArray
    fun serialize(response: Response): ByteArray
}

/**
 * Deserializer for items serialized with [Serializer].
 * @see Serializer
 */
interface Deserializer {
    fun asMessage(byteArray: ByteArray): Message
    fun asResponse(byteArray: ByteArray): Response
}
