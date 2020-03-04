package pt.pak3nuh.hollywood.processor.generator.context

class GenerationContext {

    private val map = mutableMapOf<Property<*>, Any>()

    operator fun <T: Any> set(property: Property<T>, data: T): GenerationContext {
        map[property] = data
        return this
    }

    operator fun <T> get(property: Property<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return map[property] as T?
    }

}