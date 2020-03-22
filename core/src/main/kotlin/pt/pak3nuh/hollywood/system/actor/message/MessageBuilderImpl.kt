package pt.pak3nuh.hollywood.system.actor.message

import pt.pak3nuh.hollywood.actor.message.BooleanParameter
import pt.pak3nuh.hollywood.actor.message.ByteParameter
import pt.pak3nuh.hollywood.actor.message.DoubleParameter
import pt.pak3nuh.hollywood.actor.message.FloatParameter
import pt.pak3nuh.hollywood.actor.message.IntParameter
import pt.pak3nuh.hollywood.actor.message.LongParameter
import pt.pak3nuh.hollywood.actor.message.Message
import pt.pak3nuh.hollywood.actor.message.MessageBuilder
import pt.pak3nuh.hollywood.actor.message.Parameter
import pt.pak3nuh.hollywood.actor.message.ParameterScope
import pt.pak3nuh.hollywood.actor.message.ReferenceParameter
import pt.pak3nuh.hollywood.actor.message.ShortParameter
import kotlin.reflect.KClass

fun createMessageBuilder(): MessageBuilder = MessageBuilderImpl()

internal class MessageBuilderImpl : MessageBuilder {

    private val functionIdBuilder = FunctionSignatureBuilder()
    private val parameterScope = Scope(functionIdBuilder)

    override fun parameters(block: ParameterScope.() -> Unit): MessageBuilder {
        block(parameterScope)
        return this
    }

    override fun build(functionName: String): Message =
            MessageImpl(functionIdBuilder.build(functionName), parameterScope.parameterList)
}

private class Scope(private val functionSignatureBuilder: FunctionSignatureBuilder) : ParameterScope {

    private val parameters = mutableListOf<Parameter>()

    val parameterList: List<Parameter>
        get() = parameters.toList()

    override fun param(name: String, kClass: KClass<*>, value: Any?) {
        addParameter(ReferenceParameter(name, kClass, value))
        functionSignatureBuilder.addReference(kClass, value == null)
    }

    override fun param(name: String, value: Byte) {
        addParameter(ByteParameter(name, value))
        functionSignatureBuilder.addByte()
    }

    override fun param(name: String, value: Boolean) {
        addParameter(BooleanParameter(name, value))
        functionSignatureBuilder.addBoolean()
    }

    override fun param(name: String, value: Short) {
        addParameter(ShortParameter(name, value))
        functionSignatureBuilder.addShort()
    }

    override fun param(name: String, value: Int) {
        addParameter(IntParameter(name, value))
        functionSignatureBuilder.addInt()
    }

    override fun param(name: String, value: Long) {
        addParameter(LongParameter(name, value))
        functionSignatureBuilder.addLong()
    }

    override fun param(name: String, value: Float) {
        addParameter(FloatParameter(name, value))
        functionSignatureBuilder.addFloat()
    }

    override fun param(name: String, value: Double) {
        addParameter(DoubleParameter(name, value))
        functionSignatureBuilder.addDouble()
    }

    private fun addParameter(parameter: Parameter) {
        parameters.asSequence()
                .filter { it.name == parameter.name }
                .forEach { throw IllegalArgumentException("Parameter ${it.name} already exists") }
        parameters.add(parameter)
    }
}
