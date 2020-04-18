package pt.pak3nuh.hollywood.actor.message.serializer

import pt.pak3nuh.hollywood.actor.message.Message
import pt.pak3nuh.hollywood.actor.message.Response

interface Serializer {
    fun serialize(message: Message): ByteArray
    fun serialize(response: Response): ByteArray
}

interface Deserializer {
    fun asMessage(byteArray: ByteArray): Message
    fun asResponse(byteArray: ByteArray): Response
}
