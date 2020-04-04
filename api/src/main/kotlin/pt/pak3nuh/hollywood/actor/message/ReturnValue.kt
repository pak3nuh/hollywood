package pt.pak3nuh.hollywood.actor.message

sealed class ReturnValue
object UnitReturn: ReturnValue()
data class ObjectReturn(val value: Any): ReturnValue()
data class ExceptionReturn(val exception: Exception): ReturnValue()
