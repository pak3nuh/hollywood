package pt.pak3nuh.hollywood.actor.message

import kotlin.reflect.KClass

/**
 * Separate interface to allow for extensibility.
 * Some metadata may be switched to a more friendly serializable format.
 */
interface ReferenceMetadata

interface KClassMetadata: ReferenceMetadata {
    val kClass: KClass<*>
}

data class KClassMetadataImpl(val jvmClassName: String, override val kClass: KClass<*>) : KClassMetadata {
    constructor(kClass: KClass<*>): this(jvmClassName(kClass), kClass)
}

private fun jvmClassName(kClass: KClass<*>): String {
    return requireNotNull(kClass.java.name) {
        "Qualified name not present for $kClass. Please make sure that it a valid type for serialization."
    }
}
