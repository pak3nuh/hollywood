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

    /**
     * Set of parameters that compose the message
     */
    val parameters: List<Parameter>

    /**
     * The trace contains the set of identifiers that a message went through
     */
    val trace: Set<String>
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
    fun <T: Any> param(name: String, kClass: KClass<T>, value: T?)
    fun param(name: String, value: Byte)
    fun param(name: String, value: Boolean)
    fun param(name: String, value: Short)
    fun param(name: String, value: Int)
    fun param(name: String, value: Long)
    fun param(name: String, value: Float)
    fun param(name: String, value: Double)
}

