package pt.pak3nuh.hollywood.actor.message.serializer

import pt.pak3nuh.hollywood.actor.message.Message

interface Serializer {
    fun serialize(message: Message): ByteArray
}

interface Deserializer {
    fun deserialize(byteArray: ByteArray): Message
}
