package pt.pak3nuh.hollywood.system.actor.message.serializer

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy
import org.objenesis.strategy.StdInstantiatorStrategy
import pt.pak3nuh.hollywood.actor.message.BooleanParameter
import pt.pak3nuh.hollywood.actor.message.ByteParameter
import pt.pak3nuh.hollywood.actor.message.DoubleParameter
import pt.pak3nuh.hollywood.actor.message.ExceptionReturn
import pt.pak3nuh.hollywood.actor.message.FloatParameter
import pt.pak3nuh.hollywood.actor.message.IntParameter
import pt.pak3nuh.hollywood.actor.message.KClassMetadata
import pt.pak3nuh.hollywood.actor.message.LongParameter
import pt.pak3nuh.hollywood.actor.message.Message
import pt.pak3nuh.hollywood.actor.message.Parameter
import pt.pak3nuh.hollywood.actor.message.ReferenceParameter
import pt.pak3nuh.hollywood.actor.message.Response
import pt.pak3nuh.hollywood.actor.message.ReturnValue
import pt.pak3nuh.hollywood.actor.message.ShortParameter
import pt.pak3nuh.hollywood.actor.message.UnitReturn
import pt.pak3nuh.hollywood.actor.message.ValueReturn
import java.io.ByteArrayOutputStream
import java.io.Serializable

internal class DefaultSerializer {

    private val serializer = Kryo()

    init {
        // First tries reflection, then gets creative with objenesis
        serializer.instantiatorStrategy = DefaultInstantiatorStrategy(StdInstantiatorStrategy())
        serializer.isRegistrationRequired = false // security risk of serialized class names but, oh well
        serializer.register(InternalMessage::class.java)
        // for no metadata parameters
        serializer.register(ArrayList::class.java)
        serializer.register(ReferenceParameter::class.java)
        serializer.register(BooleanParameter::class.java)
        serializer.register(ByteParameter::class.java)
        serializer.register(ShortParameter::class.java)
        serializer.register(IntParameter::class.java)
        serializer.register(LongParameter::class.java)
        serializer.register(FloatParameter::class.java)
        serializer.register(DoubleParameter::class.java)
    }

    fun serialize(message: Message): ByteArray {
        registerParameterClasses(message.parameters)
        val internalMessage = InternalMessage.from(message)
        val output = Output(ByteArrayOutputStream())
        serializer.writeObject(output, internalMessage)
        return output.toBytes()
    }

    fun serialize(response: Response): ByteArray {
        val output = Output(ByteArrayOutputStream())
        val value: Any? = when (val returnValue: ReturnValue = response.returnValue) {
            UnitReturn -> null
            is ValueReturn -> returnValue.value
            is ExceptionReturn -> returnValue.exception
        }
        if (value != null) {
            serializer.register(value::class.java)
            serializer.writeObject(output, value)
        }
        return output.toBytes()
    }

    fun deserializeMessage(byteArray: ByteArray): Message {
        val internalMessage = serializer.readObject(Input(byteArray), InternalMessage::class.java)
        return ReconstructedMessage(internalMessage.functionId, internalMessage.parameters)
    }

    fun deserializeResponse(byteArray: ByteArray): Response {
        val response = when (val value: Any? = serializer.readObject(Input(byteArray), Any::class.java)) {
            null -> UnitReturn
            is Exception -> ExceptionReturn(value)
            else -> ValueReturn(value)
        }
        return ResponseImpl(response)
    }

    private fun registerParameterClasses(parameters: List<Parameter>) {
        serializer.register(parameters.javaClass)
        parameters.asSequence()
                .filterIsInstance<ReferenceParameter>()
                .mapNotNull { it.metadata }
                .filterIsInstance<KClassMetadata>()
                .forEach {
                    serializer.register(it.kClass.java)
                }
    }
}

internal data class InternalMessage constructor(val functionId: String, val parameters: ArrayList<Parameter>) : Serializable {
    companion object {
        fun from(message: Message): InternalMessage {
            val noMetadataParameters = message.parameters.mapTo(ArrayList()) {
                if (it is ReferenceParameter) ReferenceParameter(it.name, it.value, null)
                else it
            }
            return InternalMessage(message.functionId, noMetadataParameters)
        }
    }
}

internal data class ReconstructedMessage(override val functionId: String, override val parameters: List<Parameter>): Message
internal data class ResponseImpl(override val returnValue: ReturnValue): Response
