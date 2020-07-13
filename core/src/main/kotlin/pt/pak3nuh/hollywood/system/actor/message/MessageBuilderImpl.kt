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

    private val parameterScope = Scope()

    override fun parameters(block: ParameterScope.() -> Unit): MessageBuilder {
        block(parameterScope)
        return this
    }

    override fun build(functionId: String): Message =
            MessageImpl(functionId, parameterScope.parameterList)
}

private class Scope : ParameterScope {

    private val parameters = mutableListOf<Parameter>()

    val parameterList: List<Parameter>
        get() = parameters.toList()

    override fun <T: Any> param(name: String, kClass: KClass<T>, value: T?) {
        addParameter(ReferenceParameter(name, kClass, value))
    }

    override fun param(name: String, value: Byte) {
        addParameter(ByteParameter(name, value))
    }

    override fun param(name: String, value: Boolean) {
        addParameter(BooleanParameter(name, value))
    }

    override fun param(name: String, value: Short) {
        addParameter(ShortParameter(name, value))
    }

    override fun param(name: String, value: Int) {
        addParameter(IntParameter(name, value))
    }

    override fun param(name: String, value: Long) {
        addParameter(LongParameter(name, value))
    }

    override fun param(name: String, value: Float) {
        addParameter(FloatParameter(name, value))
    }

    override fun param(name: String, value: Double) {
        addParameter(DoubleParameter(name, value))
    }

    private fun addParameter(parameter: Parameter) {
        parameters.asSequence()
                .filter { it.name == parameter.name }
                .forEach { throw IllegalArgumentException("Parameter ${it.name} already exists") }
        parameters.add(parameter)
    }
}
