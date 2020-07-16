package pt.pak3nuh.hollywood.system.actor.message.serializer

import pt.pak3nuh.hollywood.actor.message.BooleanParameter
import pt.pak3nuh.hollywood.actor.message.ByteParameter
import pt.pak3nuh.hollywood.actor.message.DoubleParameter
import pt.pak3nuh.hollywood.actor.message.ExceptionReturn
import pt.pak3nuh.hollywood.actor.message.FloatParameter
import pt.pak3nuh.hollywood.actor.message.IntParameter
import pt.pak3nuh.hollywood.actor.message.LongParameter
import pt.pak3nuh.hollywood.actor.message.Message
import pt.pak3nuh.hollywood.actor.message.ReferenceParameter
import pt.pak3nuh.hollywood.actor.message.Response
import pt.pak3nuh.hollywood.actor.message.ShortParameter
import pt.pak3nuh.hollywood.actor.message.UnitReturn
import pt.pak3nuh.hollywood.actor.message.ValueReturn
import pt.pak3nuh.hollywood.util.log.getLogger
import java.io.ByteArrayOutputStream
import java.io.Externalizable
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.OutputStream

class ExternalizableSerDes : InternalSerDes {

    fun serialize(message: Message): ByteArray {
        ByteArrayOutputStream().use { stream ->
            serialize(message, stream)
            return stream.toByteArray()
        }
    }

    override fun serialize(message: Message, stream: OutputStream) {
        ObjectOutputStream(stream).use { out ->
            ExternalizableMessage(message).writeExternal(out)
        }
    }

    override fun supports(message: Message): Boolean {
        return message.parameters.all {
            when (it) {
                is ReferenceParameter -> isValueExternalizable(it.value)
                is BooleanParameter, is ByteParameter, is ShortParameter, is IntParameter, is LongParameter,
                is FloatParameter, is DoubleParameter -> true
            }
        }
    }

    fun serialize(response: Response): ByteArray {
        ByteArrayOutputStream().use { stream ->
            serialize(response, stream)
            return stream.toByteArray()
        }
    }

    override fun serialize(response: Response, stream: OutputStream) {
        ObjectOutputStream(stream).use { out ->
            ExternalizableResponse(response).writeExternal(out)
        }
    }

    override fun supports(response: Response): Boolean {
        return when (val value = response.returnValue) {
            is UnitReturn, is ExceptionReturn -> true
            is ValueReturn -> isValueExternalizable(value.value)
        }
    }

    override fun deserializeMessage(stream: InputStream): Message {
        ObjectInputStream(stream).use { input ->
            return ExternalizableMessage(input).message
        }
    }

    override fun deserializeResponse(stream: InputStream): Response {
        ObjectInputStream(stream).use { input ->
            return ExternalizableResponse(input).response
        }
    }

    // arrays and boxed values are not adapted by design
    private fun isValueExternalizable(value: Any?): Boolean {
        val result = value == null || value is Externalizable
        logger.trace("Evaluating is serializable on {}: {}", value, result)
        return result
    }

    private companion object {
        val logger = getLogger<ExternalizableSerDes>()
    }
}
