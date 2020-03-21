package pt.pak3nuh.hollywood.actor.message

import kotlinx.coroutines.CompletableDeferred

interface Message {
    val functionId: String
    val parameters: List<Parameter>
    val returnValue: CompletableDeferred<ReturnValue>
}

sealed class Parameter {
    abstract val name: String
}

data class ReferenceParameter(override val name: String, val value: Any): Parameter()
data class BooleanParameter(override val name: String, val value: Boolean): Parameter()
data class ByteParameter(override val name: String, val value: Byte): Parameter()
data class ShortParameter(override val name: String, val value: Short): Parameter()
data class IntParameter(override val name: String, val value: Int): Parameter()
data class LongParameter(override val name: String, val value: Long): Parameter()
data class FloatParameter(override val name: String, val value: Float): Parameter()
data class DoubleParameter(override val name: String, val value: Double): Parameter()

sealed class ReturnValue
object UnitReturn: ReturnValue()
data class ObjectReturn(val value: Any): ReturnValue()
data class ExceptionReturn(val exception: Exception): ReturnValue()
