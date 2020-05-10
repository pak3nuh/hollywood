package pt.pak3nuh.hollywood.actor.message

sealed class ReturnValue

/**
 * A special response of [Unit].
 */
@Suppress("CanSealedSubClassBeObject") // serializers don't like object definitions
class UnitReturn: ReturnValue()

/**
 * Successful response with a value.
 */
// specialize primitives for optimization if needed
data class ValueReturn(val value: Any?): ReturnValue()

/**
 * Failed function execution that ended with an exception being thrown.
 */
// exceptions can lead to serialization overflow
data class ExceptionReturn(
        val klass: Class<out Exception>,
        val message: String?,
        val stackTrace: List<StackTraceElement>?
): ReturnValue() {
    constructor(exception: Exception): this(exception::class.java, exception.message, exception.stackTrace?.toList())
}
