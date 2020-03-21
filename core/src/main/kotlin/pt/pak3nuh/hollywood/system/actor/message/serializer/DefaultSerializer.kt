package pt.pak3nuh.hollywood.system.actor.message.serializer

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy
import org.objenesis.strategy.StdInstantiatorStrategy
import pt.pak3nuh.hollywood.actor.message.BooleanParameter
import pt.pak3nuh.hollywood.actor.message.ByteParameter
import pt.pak3nuh.hollywood.actor.message.DoubleParameter
import pt.pak3nuh.hollywood.actor.message.FloatParameter
import pt.pak3nuh.hollywood.actor.message.IntParameter
import pt.pak3nuh.hollywood.actor.message.LongParameter
import pt.pak3nuh.hollywood.actor.message.Message
import pt.pak3nuh.hollywood.actor.message.Parameter
import pt.pak3nuh.hollywood.actor.message.ReferenceParameter
import pt.pak3nuh.hollywood.actor.message.ShortParameter
import java.io.ByteArrayOutputStream
import java.io.Serializable

internal class DefaultSerializer {

    private val serializer = Kryo()

    init {
        // First tries reflection, then gets creative with objenesis
        serializer.instantiatorStrategy = DefaultInstantiatorStrategy(StdInstantiatorStrategy())
        serializer.register(InternalMessage::class.java)
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
        val internalMessage = InternalMessage(message.functionId, message.parameters)
        registerParameters(message.parameters)
        val output = Output(ByteArrayOutputStream())
        serializer.writeObject(output, internalMessage)
        return output.toBytes()
    }

    private fun registerParameters(parameters: List<Parameter>) {
        serializer.register(parameters.javaClass)
        parameters.asSequence()
                .filterIsInstance<ReferenceParameter>()
                .forEach {
                    serializer.register(it.value.javaClass)
                }
    }

    fun deserialize(byteArray: ByteArray): InternalMessage {
        return serializer.readObject(Input(byteArray), InternalMessage::class.java)
    }
}

internal data class InternalMessage(val functionId: String, val parameters: List<Parameter>) : Serializable
