@file:Suppress("FunctionName")

package pt.pak3nuh.hollywood.actor.message

/**
 * Generalization of an actor response to a message sent.
 *
 * It can have one of three return values.
 * 1. [ReturnType.UNIT] or no value.
 * 2. [ReturnType.VALUE] if a value is expected, including null.
 * 3. [ReturnType.EXCEPTION] if an exception occurred.
 *
 * Check [UnitResponse], [ValueResponse], [ExceptionResponse] for factory functions.
 */
interface Response {
    val returnType: ReturnType
    val returnValue: ReturnValue
}

/**
 * Expresses the type of the value returned in a serializable safe way. Some serializers break the semantics of
 * kotlin language constructs like object definitions.
 */
enum class ReturnType {
    /**
     * No value, [UnitReturn]
     */
    UNIT,

    /**
     * Holds a value or null, [ValueReturn]
     */
    VALUE,

    /**
     * Holds an exception, [ExceptionReturn]
     */
    EXCEPTION
}

private val unitReturnValue = UnitReturn()
fun ValueResponse(returnValue: Any?): Response = ResponseImpl(ReturnType.VALUE, ValueReturn(returnValue))
fun UnitResponse(): Response = ResponseImpl(ReturnType.UNIT, unitReturnValue)
fun ExceptionResponse(exception: Exception): Response = ResponseImpl(ReturnType.EXCEPTION, ExceptionReturn(exception))

private data class ResponseImpl(override val returnType: ReturnType, override val returnValue: ReturnValue) : Response
