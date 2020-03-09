package pt.pak3nuh.hollywood.processor.generator.context

import pt.pak3nuh.hollywood.processor.generator.util.Logger
import javax.lang.model.type.TypeMirror
import javax.lang.model.type.WildcardType
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import kotlin.coroutines.Continuation

class GenerationContext(val logger: Logger, private val types: Types, private val elements: Elements) {

    private val map = mutableMapOf<Property<*>, Any>()
    private val objectType = getTypeMirror(java.lang.Object::class.java)

    /**
     * Type to check if another type is a coroutine
     */
    private val continuationType = buildCoroutineType()

    /**
     * Type returned at the bytecode level
     */
    val coroutineJvmReturnType: TypeMirror = objectType

    val unitType = getTypeMirror(Unit::class.java)

    private fun getTypeMirror(clazz: Class<*>): TypeMirror = elements.getTypeElement(clazz.canonicalName).asType()

    operator fun <T : Any> set(property: Property<T>, data: T): GenerationContext {
        map[property] = data
        return this
    }

    operator fun <T> get(property: Property<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return map[property] as T?
    }

    fun isAssignable(t1: TypeMirror, t2: TypeMirror): Boolean {
        logger.logDebug("Invoking is assignable between [$t1] and [$t2]")
        return types.isAssignable(t1, t2)
    }

    fun isAssignableCoroutine(typeMirror: TypeMirror): Boolean {
        return isAssignable(continuationType, typeMirror)
    }

    private fun buildCoroutineType(): TypeMirror {
        val upperBound: WildcardType = types.getWildcardType(null, objectType)
        val continuationElement = elements.getTypeElement(Continuation::class.java.canonicalName)
        // equals to Continuation<in java.lang.Object>
        return types.getDeclaredType(continuationElement, upperBound)
    }

}
