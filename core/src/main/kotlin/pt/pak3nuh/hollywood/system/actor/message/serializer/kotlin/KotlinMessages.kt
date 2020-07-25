package pt.pak3nuh.hollywood.system.actor.message.serializer.kotlin

import kotlinx.serialization.Serializable

@Serializable
internal data class KSerDesMessage(val functionId: String, val parameters: List<KSerDesParameter>)

@Serializable
internal data class KSerDesParameter(
        val type: Type,
        val name: String,
        val boolean: Boolean = false,
        val byte: Byte = 0.toByte(),
        val short: Short = 0.toShort(),
        val int: Int = 0,
        val long: Long = 0L,
        val float: Float = 0f,
        val double: Double = 0.0,
        val reference: KSerDesValue? = null
) {
    enum class Type {
        Boolean, Byte, Short, Int, Long, Float, Double, Ref
    }
}

@Serializable
internal data class KSerDesResponse(
        val value: KSerDesValue?
)

@Serializable
internal data class KSerDesValue(
        val className: String,
        val bytes: ByteArray
)
