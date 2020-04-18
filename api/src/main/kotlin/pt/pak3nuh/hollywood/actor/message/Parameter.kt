package pt.pak3nuh.hollywood.actor.message

import kotlin.reflect.KClass

sealed class Parameter {
    abstract val name: String
}

data class ReferenceParameter(
        override val name: String,
        val value: Any?,
        val metadata: ReferenceMetadata?
): Parameter() {
    constructor(name: String, value: Any): this(name, value::class, value)
    constructor(name: String, kclass: KClass<*>, value: Any?): this(name, value, KClassMetadataImpl(kclass))

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ReferenceParameter

        if (name != other.name) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (value?.hashCode() ?: 0)
        return result
    }


}

// nullable primitives are reference types
data class BooleanParameter(override val name: String, val value: Boolean): Parameter()
data class ByteParameter(override val name: String, val value: Byte): Parameter()
data class ShortParameter(override val name: String, val value: Short): Parameter()
data class IntParameter(override val name: String, val value: Int): Parameter()
data class LongParameter(override val name: String, val value: Long): Parameter()
data class FloatParameter(override val name: String, val value: Float): Parameter()
data class DoubleParameter(override val name: String, val value: Double): Parameter()

