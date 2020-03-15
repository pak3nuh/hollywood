package pt.pak3nuh.hollywood.processor.generator.context

import pt.pak3nuh.hollywood.processor.generator.types.TypeUtil
import pt.pak3nuh.hollywood.processor.generator.util.Logger

interface GenerationContext {
    val logger: Logger
    val typeUtil: TypeUtil

    operator fun <T : Any> set(property: Property<T>, data: T): GenerationContext
    operator fun <T> get(property: Property<T>): T?
}

class GenerationContextImpl(override val logger: Logger, override val typeUtil: TypeUtil): GenerationContext {

    private val map = mutableMapOf<Property<*>, Any>()

   override operator fun <T : Any> set(property: Property<T>, data: T): GenerationContext {
        map[property] = data
        return this
    }

    override operator fun <T> get(property: Property<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return map[property] as T?
    }

}
