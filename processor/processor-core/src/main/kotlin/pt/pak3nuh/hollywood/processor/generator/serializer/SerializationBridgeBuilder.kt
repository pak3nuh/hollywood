package pt.pak3nuh.hollywood.processor.generator.serializer

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SET
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import kotlinx.serialization.KSerializer
import pt.pak3nuh.hollywood.processor.api.GeneratedSerializerProvider
import pt.pak3nuh.hollywood.processor.api.SerializerData
import javax.lang.model.element.Element
import kotlin.reflect.KClass

class SerializationBridgeBuilder(className: ClassName) {

    private val typeBuilder = TypeSpec.classBuilder(className)
    private val serializerDataClassName = SerializerData::class.asClassName()
    private val allSerializers = FunSpec.builder("getAllSerializers")
            .addModifiers(KModifier.OVERRIDE)
            .returns(SET.parameterizedBy(serializerDataClassName))

    private val typeVariable = TypeVariableName("T", Any::class)
    private val getSerializer = FunSpec.builder("getSerializer")
            .addModifiers(KModifier.OVERRIDE)
            .addTypeVariable(typeVariable)
            .addParameter("kClass", KClass::class.asClassName().parameterizedBy(typeVariable))
            .returns(KSerializer::class.asClassName().parameterizedBy(typeVariable).copy(nullable = true))

    private val bridgesToAdd = mutableSetOf<TypeName>()

    fun build(): TypeSpec {
        return typeBuilder.addSuperinterface(GeneratedSerializerProvider::class)
                .addFunction(allSerializers.addCode(allSerializersBlock()).build())
                .addFunction(getSerializer.addCode(getSerializerBlock()).build())
                .build()
    }

    private fun getSerializerBlock(): CodeBlock {
        // string comparison generates more efficient bytecode but raises equal named class problems
        return CodeBlock.builder()
                .beginControlFlow("return when(kClass)").apply {
                    bridgesToAdd.forEach { typeName ->
                        addStatement("%T::class -> %T.serializer()", typeName, typeName)
                    }
                    addStatement("else -> null")
                }
                .endControlFlow()
                .addStatement("as KSerializer<T>?")
                .build()
    }

    private fun allSerializersBlock(): CodeBlock {
        return CodeBlock.builder()
                .add("return setOf(")
                .indent().apply {
                    bridgesToAdd.forEachIndexed { index, typeName ->
                        val comma = if (index == 0) "" else ","
                        addStatement("%L %T(%T::class, %T.serializer())", comma, serializerDataClassName, typeName, typeName)
                    }
                }
                .unindent()
                .add(")")
                .build()
    }

    fun addBridges(annotatedElements: Set<Element>): SerializationBridgeBuilder {
        bridgesToAdd.addAll(annotatedElements.map { it.asType().asTypeName() })
        return this
    }
}