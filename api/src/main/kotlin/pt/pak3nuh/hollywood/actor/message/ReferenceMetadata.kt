package pt.pak3nuh.hollywood.actor.message

import kotlin.reflect.KClass

/**
 * Separate interface to allow for extensibility.
 * Some metadata may be switched to a more friendly serializable format.
 */
interface ReferenceMetadata {
    val jvmClassName: String
}

data class KClassMetadata(override val jvmClassName: String, val kClass: KClass<*>) : ReferenceMetadata {
    constructor(kClass: KClass<*>): this(jvmClassName(kClass), kClass)
}

private fun jvmClassName(kClass: KClass<*>): String {
    return requireNotNull(kClass.java.name) {
        "Qualified name not present for $kClass. Please make sure that it a valid type for serialization."
    }
}
