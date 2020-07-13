@file:Suppress("FunctionName")

package pt.pak3nuh.hollywood.system.actor.message.serializer

import pt.pak3nuh.hollywood.actor.message.Message
import pt.pak3nuh.hollywood.actor.message.Response
import pt.pak3nuh.hollywood.actor.message.serializer.Deserializer
import pt.pak3nuh.hollywood.actor.message.serializer.Serializer

private val serializerStrategy = SerializerStrategy(DefaultSerializer())
fun Serializer(): Serializer = serializerStrategy
fun Deserializer(): Deserializer = serializerStrategy

// in the future this will change to a much more advanced strategy
// the dream is to serialize each parameter with the best serializer and with a single byte array
// todo use externalizable serializer
internal class SerializerStrategy(private val defaultSerializer: DefaultSerializer): Serializer, Deserializer {
    override fun serialize(message: Message): ByteArray = defaultSerializer.serialize(message)
    override fun serialize(response: Response): ByteArray = defaultSerializer.serialize(response)
    override fun asMessage(byteArray: ByteArray): Message = defaultSerializer.deserializeMessage(byteArray)
    override fun asResponse(byteArray: ByteArray): Response = defaultSerializer.deserializeResponse(byteArray)
    override fun supports(message: Message): Boolean = true
    override fun supports(message: Response): Boolean = true
}

enum class StrategyType {
    Default, Externalizable
}