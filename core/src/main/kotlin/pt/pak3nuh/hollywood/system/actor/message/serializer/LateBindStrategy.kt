@file:Suppress("FunctionName")

package pt.pak3nuh.hollywood.system.actor.message.serializer

import pt.pak3nuh.hollywood.actor.message.Message
import pt.pak3nuh.hollywood.actor.message.Response
import pt.pak3nuh.hollywood.actor.message.serializer.Deserializer
import pt.pak3nuh.hollywood.actor.message.serializer.Serializer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

private val serializerStrategy = LateBindStrategy(DefaultSerializer(), ExternalizableSerDes())
fun Serializer(): Serializer = serializerStrategy
fun Deserializer(): Deserializer = serializerStrategy

// in the future this will change to a much more advanced strategy
// the dream is to serialize each parameter with the best serializer and with a single byte array
/**
 * Messaging strategy that decides the serializer on each message.
 */
internal class LateBindStrategy(
        private val defaultSerializer: InternalSerDes,
        private val externalizableSerializer: InternalSerDes
) : Serializer, Deserializer {
    override fun serialize(message: Message): ByteArray {
        ByteArrayOutputStream().use {
            when {
                externalizableSerializer.supports(message) -> {
                    it.write(StrategyType.Externalizable.ordinal)
                    externalizableSerializer.serialize(message, it)
                }
                else -> {
                    it.write(StrategyType.Default.ordinal)
                    defaultSerializer.serialize(message, it)
                }
            }
            return it.toByteArray()
        }
    }

    override fun serialize(response: Response): ByteArray {
        ByteArrayOutputStream().use {
            when {
                externalizableSerializer.supports(response) -> {
                    it.write(StrategyType.Externalizable.ordinal)
                    externalizableSerializer.serialize(response, it)
                }
                else -> {
                    it.write(StrategyType.Default.ordinal)
                    defaultSerializer.serialize(response, it)
                }
            }
            return it.toByteArray()
        }
    }

    override fun asMessage(byteArray: ByteArray): Message {
        ByteArrayInputStream(byteArray).use {
            val type = it.read()
            return when (StrategyType.values()[type]) {
                StrategyType.Externalizable -> externalizableSerializer.deserializeMessage(it)
                StrategyType.Default -> defaultSerializer.deserializeMessage(it)
            }
        }
    }
    override fun asResponse(byteArray: ByteArray): Response {
        ByteArrayInputStream(byteArray).use {
            val type = it.read()
            return when (StrategyType.values()[type]) {
                StrategyType.Externalizable -> externalizableSerializer.deserializeResponse(it)
                StrategyType.Default -> defaultSerializer.deserializeResponse(it)
            }
        }
    }

    internal enum class StrategyType {
        Default, Externalizable
    }
}
