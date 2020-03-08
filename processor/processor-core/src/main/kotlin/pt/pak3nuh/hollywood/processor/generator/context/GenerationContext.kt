package pt.pak3nuh.hollywood.processor.generator.context

import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import pt.pak3nuh.hollywood.processor.generator.util.Logger
import javax.lang.model.type.TypeMirror
import javax.lang.model.type.WildcardType
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import kotlin.coroutines.Continuation

class GenerationContext(val logger: Logger, private val types: Types, private val elements: Elements) {

    private val map = mutableMapOf<Property<*>, Any>()
    private val objectType = getTypeMirror(java.lang.Object::class.java)
    private val kotlinConversionMap = mapOf<TypeName, ClassName>(
            java.lang.Boolean::class.java.asTypeName() to BOOLEAN,
            java.lang.Byte::class.java.asTypeName() to BYTE,
            java.lang.Short::class.java.asTypeName() to SHORT,
            java.lang.Integer::class.java.asTypeName() to INT,
            java.lang.Long::class.java.asTypeName() to LONG,
            java.lang.Float::class.java.asTypeName() to FLOAT,
            java.lang.Double::class.java.asTypeName() to DOUBLE,
            java.lang.Boolean.TYPE.asTypeName() to BOOLEAN,
            java.lang.Byte.TYPE.asTypeName() to BYTE,
            java.lang.Short.TYPE.asTypeName() to SHORT,
            java.lang.Integer.TYPE.asTypeName() to INT,
            java.lang.Long.TYPE.asTypeName() to LONG,
            java.lang.Float.TYPE.asTypeName() to FLOAT,
            java.lang.Double.TYPE.asTypeName() to DOUBLE,
            // immutable because the contents shouldn't change between calls
            // todo add documentation and test cases
            java.util.List::class.java.asTypeName() to List::class.asClassName(),
            java.util.Set::class.java.asTypeName() to Set::class.asClassName(),
            java.util.Map::class.java.asTypeName() to Map::class.asClassName()
    )

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

    fun asKotlinTypeName(javaType: TypeMirror): TypeName {
        val javaTypeName = javaType.asTypeName()
        return when (javaTypeName) {
            is ClassName -> kotlinConversionMap[javaTypeName]
            is ParameterizedTypeName -> {
                // todo only one level deep of parameterization is being addressed
                kotlinConversionMap[javaTypeName.rawType]?.parameterizedBy(javaTypeName.typeArguments)
            }
            else -> null
        } ?: javaTypeName

    }

}
