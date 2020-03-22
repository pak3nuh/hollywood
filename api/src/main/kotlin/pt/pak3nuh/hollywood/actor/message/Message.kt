package pt.pak3nuh.hollywood.actor.message

import kotlinx.coroutines.CompletableDeferred
import kotlin.reflect.KClass

interface Message {
    val functionId: String
    val parameters: List<Parameter>
    val returnValue: CompletableDeferred<ReturnValue>
}

interface MessageBuilder {
    fun parameters(block: ParameterScope.() -> Unit): MessageBuilder
    fun build(functionName: String): Message
}

interface ParameterScope {
    fun param(name: String, kClass: KClass<*>, value: Any?)
    fun param(name: String, value: Any) = param(name, value::class, value)
    fun param(name: String, value: Byte)
    fun param(name: String, value: Boolean)
    fun param(name: String, value: Short)
    fun param(name: String, value: Int)
    fun param(name: String, value: Long)
    fun param(name: String, value: Float)
    fun param(name: String, value: Double)
}

