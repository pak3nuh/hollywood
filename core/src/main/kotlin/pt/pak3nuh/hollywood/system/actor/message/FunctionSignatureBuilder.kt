package pt.pak3nuh.hollywood.system.actor.message

import kotlin.reflect.KClass

internal class FunctionSignatureBuilder {

    private val parcels = mutableListOf<String>()

    fun build(functionName: String): String {
        require(functionName.isNotBlank()) { "Function name must be valid" }
        return "$functionName:${parcels.joinToString(";")}"
    }

    // no need for nesting here because of erasure
    // technically arrays can be used here but they shouldn't
    fun addReference(kClass: KClass<*>, isNull: Boolean): FunctionSignatureBuilder {
        parcels.add(referenceString(isNull, kClass))
        return this
    }

    fun addArray(nesting: (NestingScope.() -> Unit)): FunctionSignatureBuilder {
        parcels.add(Scope().apply(nesting).componentString)
        return this
    }

    fun addByte(): FunctionSignatureBuilder {
        parcels.add("B")
        return this
    }

    fun addBoolean(): FunctionSignatureBuilder {
        parcels.add("Z")
        return this
    }

    fun addShort(): FunctionSignatureBuilder {
        parcels.add("S")
        return this
    }

    fun addInt(): FunctionSignatureBuilder {
        parcels.add("I")
        return this
    }

    fun addLong(): FunctionSignatureBuilder {
        parcels.add("J")
        return this
    }

    fun addFloat(): FunctionSignatureBuilder {
        parcels.add("F")
        return this
    }

    fun addDouble(): FunctionSignatureBuilder {
        parcels.add("D")
        return this
    }

    internal interface NestingScope {
        fun component(kClass: KClass<*>, isNull: Boolean)
        fun nest(isNull: Boolean, nesting: (NestingScope.() -> Unit))
    }

    private class Scope: NestingScope {

        var componentString: String = arrayString(false, referenceString(false, Any::class))
        private var nestCalled = false

        override fun component(kClass: KClass<*>, isNull: Boolean) {
            componentString = referenceString(isNull, kClass)
        }

        override fun nest(isNull: Boolean, nesting: (NestingScope.() -> Unit)) {
            check(!nestCalled) { "Nest can only be called once per scope" }
            componentString = arrayString(isNull, Scope().apply(nesting).componentString)
            nestCalled = true
        }

    }

}

private fun referenceString(isNull: Boolean, kClass: KClass<*>): String {
    val nullable = if (isNull) "?" else ""
    return "L${kClass.qualifiedName}$nullable"
}

private fun arrayString(isNull: Boolean, content: String): String {
    val nullable = if (isNull) "?" else ""
    return "[$content]$nullable"
}
