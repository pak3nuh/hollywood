package pt.pak3nuh.hollywood.processor.generator.context

import pt.pak3nuh.hollywood.processor.generator.mirror.TypeUtil
import pt.pak3nuh.hollywood.processor.generator.util.Logger

interface GenerationContext {
    val logger: Logger
    val typeUtil: TypeUtil
    val useMetadata: Boolean

    operator fun <T : Any> set(property: Property<T>, data: T): GenerationContext
    operator fun <T> get(property: Property<T>): T?
    fun <T> remove(property: Property<T>): T?
}

@Suppress("UNCHECKED_CAST")
class GenerationContextImpl(
        override val logger: Logger,
        override val typeUtil: TypeUtil,
        override val useMetadata: Boolean
): GenerationContext {

    private val map = mutableMapOf<Property<*>, Any>()

   override operator fun <T : Any> set(property: Property<T>, data: T): GenerationContext {
        map[property] = data
        return this
    }

    override operator fun <T> get(property: Property<T>): T? {
        return map[property] as T?
    }

    override fun <T> remove(property: Property<T>): T? {
        return map.remove(property) as T?
    }
}
