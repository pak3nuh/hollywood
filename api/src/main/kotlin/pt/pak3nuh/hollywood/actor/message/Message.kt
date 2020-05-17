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
    /**
     * @throws IllegalArgumentException If it is used with an [Array].
     */
    fun param(name: String, kClass: KClass<*>, isNull: Boolean, value: Any?)
    fun param(name: String, value: Any, isNull: Boolean) = param(name, value::class, isNull, value)

    /**
     * Declares an array or matrix of type [component]. A matrix is a multi level array starting at L0.
     * @param name The param name.
     * @param component The array component.
     * @param value The array instance.
     * @param nullable The nullability declaration of every array level. The first element is for the array L0
     * and the last element is for the component. There should be at least 2 elements.
     * @throws IllegalArgumentException If the [nullable] has less than 2 elements.
     */
    fun arrayParam(name: String, component: KClass<*>, value: Any?, vararg nullable: Boolean)
    fun param(name: String, value: Byte)
    fun param(name: String, value: Boolean)
    fun param(name: String, value: Short)
    fun param(name: String, value: Int)
    fun param(name: String, value: Long)
    fun param(name: String, value: Float)
    fun param(name: String, value: Double)
}

