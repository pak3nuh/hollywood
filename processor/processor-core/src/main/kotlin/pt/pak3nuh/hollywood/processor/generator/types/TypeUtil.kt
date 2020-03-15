package pt.pak3nuh.hollywood.processor.generator.types

import com.squareup.kotlinpoet.TypeName
import pt.pak3nuh.hollywood.actor.proxy.ActorProxyBase
import pt.pak3nuh.hollywood.processor.generator.util.Logger
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.type.WildcardType
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import kotlin.coroutines.Continuation

interface TypeUtil {

    val coroutineJvmReturnType: TypeMirror
    val unitType: TypeMirror

    fun isAssignable(t1: TypeMirror, t2: TypeMirror): Boolean
    fun isAssignableCoroutine(typeMirror: TypeMirror): Boolean
    fun getElementValuesWithDefaults(annotation: AnnotationMirror): Map<out ExecutableElement, AnnotationValue>

    fun convert(typeMirror: TypeMirror): TypeName
    fun convert(element: Element): TypeName = convert(element.asType())
    fun isValidProxy(typeMirror: TypeMirror): Boolean
}

class TypeUtilImpl(
        private val logger: Logger,
        private val types: Types,
        private val elements: Elements,
        private val typeConverter: TypeConverter
): TypeUtil {

    private val objectType = getTypeMirror(java.lang.Object::class.java)
    /**
     * Type to check if another type is a coroutine
     */
    private val continuationType = buildCoroutineType()

    /**
     * Type returned at the bytecode level
     */
    override val coroutineJvmReturnType: TypeMirror = objectType

    override val unitType = getTypeMirror(Unit::class.java)

    private val customProxy: TypeMirror = getTypeMirror(ActorProxyBase::class.java)

    override fun isAssignable(t1: TypeMirror, t2: TypeMirror): Boolean {
        logger.logDebug("Invoking is assignable between [$t1] and [$t2]")
        return types.isAssignable(t1, t2)
    }

    override fun isAssignableCoroutine(typeMirror: TypeMirror): Boolean {
        return isAssignable(continuationType, typeMirror)
    }

    override fun convert(typeMirror: TypeMirror): TypeName {
        return typeConverter.convert(typeMirror)
    }

    override fun getElementValuesWithDefaults(annotation: AnnotationMirror): Map<out ExecutableElement, AnnotationValue> {
        return elements.getElementValuesWithDefaults(annotation)
    }

    override fun isValidProxy(typeMirror: TypeMirror): Boolean = isAssignable(typeMirror, customProxy)

    private fun getTypeMirror(clazz: Class<*>): TypeMirror = elements.getTypeElement(clazz.canonicalName).asType()

    private fun buildCoroutineType(): TypeMirror {
        val upperBound: WildcardType = types.getWildcardType(null, objectType)
        val continuationElement = elements.getTypeElement(Continuation::class.java.canonicalName)
        // equals to Continuation<in java.lang.Object>
        return types.getDeclaredType(continuationElement, upperBound)
    }
}
