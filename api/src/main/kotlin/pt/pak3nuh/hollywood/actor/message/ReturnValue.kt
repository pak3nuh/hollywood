package pt.pak3nuh.hollywood.actor.message

sealed class ReturnValue
object UnitReturn: ReturnValue()
// specialize primitives for optimization if needed
data class ValueReturn(val value: Any?): ReturnValue()
data class ExceptionReturn(val exception: Exception): ReturnValue()
