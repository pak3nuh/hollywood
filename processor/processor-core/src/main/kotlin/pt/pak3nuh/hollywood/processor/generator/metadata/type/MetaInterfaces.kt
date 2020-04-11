package pt.pak3nuh.hollywood.processor.generator.metadata.type

import com.squareup.kotlinpoet.TypeName
import pt.pak3nuh.hollywood.processor.generator.context.Property
import kotlin.reflect.KClass

/**
 * Interpretation of the [Metadata] emitted by the kotlin compiler. This metadata contain a lot more information
 * than the mirror API provided by the java compiler, but it has not reached a stable point.
 *
 * For that reason **javac** should remain the main source of information.
 */
interface MetaClass {
    val functions: List<MetaFun>
    val name: String

    companion object Key : Property<MetaClass>
}

interface MetaFun {
    val parameters: List<MetaParameter>
    val isSuspend: Boolean
    val name: String
    val returnType: MetaType
}

interface MetaParameter {
    val name: String
    val type: MetaType
}

interface MetaType {
    val name: String
    // bound to Kotlin Poet to cut over engineering. can be abstracted if necessary
    fun asTypeName(): TypeName
    fun hasAnnotation(kClass: KClass<out Annotation>): Boolean
}

