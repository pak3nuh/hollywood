package pt.pak3nuh.hollywood.system.actor.message.serializer

import pt.pak3nuh.hollywood.actor.message.Message
import pt.pak3nuh.hollywood.actor.message.serializer.Serializer

// in the future this will change to a much more advanced strategy
// the dream is to serialize each parameter with the best serializer and with a single byte array
internal class SerializerStrategy(private val defaultSerializer: DefaultSerializer): Serializer {
    override fun serialize(message: Message): ByteArray = defaultSerializer.serialize(message)
}

fun buildStrategy(): Serializer = SerializerStrategy(DefaultSerializer())
