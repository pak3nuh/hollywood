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
import pt.pak3nuh.hollywood.actor.message.ReturnType
import pt.pak3nuh.hollywood.actor.message.ReturnValue
import pt.pak3nuh.hollywood.actor.message.ShortParameter
import pt.pak3nuh.hollywood.actor.message.UnitReturn
import pt.pak3nuh.hollywood.actor.message.ValueReturn
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

internal class DefaultSerializer: InternalSerDes {

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

        serializer.register(ReturnType::class.java)
        serializer.register(InternalResponse::class.java)
        serializer.register(UnitReturn::class.java)
        serializer.register(ValueReturn::class.java)
        serializer.register(ExceptionReturn::class.java)
    }

    fun serialize(message: Message): ByteArray {
        ByteArrayOutputStream().use { stream ->
            serialize(message, stream)
            return stream.toByteArray()
        }

    }

    override fun serialize(message: Message, stream: OutputStream) {
        registerParameterClasses(message.parameters)
        val internalMessage = InternalMessage.from(message)
        Output(stream).use { output ->
            serializer.writeClassAndObject(output, internalMessage)
            output.flush()
        }
    }

    fun serialize(response: Response): ByteArray {
        ByteArrayOutputStream().use { stream ->
            serialize(response, stream)
            return stream.toByteArray()
        }
    }

    override fun serialize(response: Response, stream: OutputStream) {
        Output(stream).use { output ->
            val internal = InternalResponse(response.returnType, response.returnValue)
            serializer.writeClassAndObject(output, internal)
            output.flush()
        }
    }

    override fun supports(message: Message): Boolean {
        return true
    }

    override fun supports(response: Response): Boolean {
        return response.returnType == ReturnType.VALUE
    }


    override fun deserializeMessage(stream: InputStream): Message {
        Input(stream).use {
            return serializer.readClassAndObject(it) as InternalMessage
        }
    }


    override fun deserializeResponse(stream: InputStream): Response {
        Input(stream).use {
            return serializer.readClassAndObject(it) as InternalResponse
        }
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

internal data class InternalMessage constructor(
        override val functionId: String,
        override val parameters: ArrayList<Parameter>,
        override val trace: Set<String>
) : Message {
    companion object {
        fun from(message: Message): InternalMessage {
            val noMetadataParameters = message.parameters.mapTo(ArrayList()) {
                if (it is ReferenceParameter) ReferenceParameter(it.name, it.value, null)
                else it
            }
            return InternalMessage(message.functionId, noMetadataParameters, message.trace)
        }
    }
}
internal data class InternalResponse(
        override val returnType: ReturnType,
        override val returnValue: ReturnValue
): Response
