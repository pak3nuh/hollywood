package pt.pak3nuh.hollywood.actor.message

import kotlin.reflect.KClass

/**
 * Message that encapsulates a function call. Much like function signatures, parameter order matter.
 */
interface Message {
    /**
     * Unique identifier for a function within an actor interface.
     */
    val functionId: String
    val parameters: List<Parameter>
}

/**
 * Builder to create [Message] instances.
 */
interface MessageBuilder {
    /**
     * Starts a scope for defining parameters.
     * @see ParameterScope
     */
    fun parameters(block: ParameterScope.() -> Unit): MessageBuilder
    fun build(functionId: String): Message
}

interface ParameterScope {
    fun param(name: String, kClass: KClass<*>, value: Any?)
    fun param(name: String, value: Byte)
    fun param(name: String, value: Boolean)
    fun param(name: String, value: Short)
    fun param(name: String, value: Int)
    fun param(name: String, value: Long)
    fun param(name: String, value: Float)
    fun param(name: String, value: Double)
}

