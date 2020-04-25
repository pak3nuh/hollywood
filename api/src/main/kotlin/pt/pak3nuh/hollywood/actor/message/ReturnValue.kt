package pt.pak3nuh.hollywood.actor.message

sealed class ReturnValue
object UnitReturn: ReturnValue()
// specialize primitives for optimization if needed
data class ValueReturn(val value: Any?): ReturnValue()
// exceptions can lead to serialization overflow
data class ExceptionReturn(val message: String?, val stackTrace: List<StackTraceElement>?): ReturnValue() {
    constructor(exception: Exception): this(exception.message, exception.stackTrace?.toList())
}
