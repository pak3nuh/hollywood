package pt.pak3nuh.hollywood.processor.generator.mirror

import com.squareup.kotlinpoet.ARRAY
import com.squareup.kotlinpoet.BOOLEAN_ARRAY
import com.squareup.kotlinpoet.BYTE_ARRAY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.DOUBLE_ARRAY
import com.squareup.kotlinpoet.FLOAT_ARRAY
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT_ARRAY
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LONG_ARRAY
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.SHORT_ARRAY
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import pt.pak3nuh.hollywood.actor.proxy.HandlerBuilder
import pt.pak3nuh.hollywood.actor.proxy.MessageHandler
import pt.pak3nuh.hollywood.actor.proxy.ProxyConfiguration
import pt.pak3nuh.hollywood.processor.Actor
import pt.pak3nuh.hollywood.processor.generator.Bundle
import pt.pak3nuh.hollywood.processor.generator.FileGenerator
import pt.pak3nuh.hollywood.processor.generator.FileWriter
import pt.pak3nuh.hollywood.processor.generator.MethodResult
import pt.pak3nuh.hollywood.processor.generator.TypeResult
import pt.pak3nuh.hollywood.processor.generator.context.GenerationContext
import pt.pak3nuh.hollywood.processor.generator.context.generationAnnotation
import pt.pak3nuh.hollywood.processor.generator.mirror.visitor.TypeElementVisitor
import pt.pak3nuh.hollywood.processor.generator.util.proxyName
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

abstract class ProxyClassGenerator : FileGenerator, TypeElementVisitor() {

    final override fun generate(element: TypeElement, context: GenerationContext): FileWriter {
        require(element.kind == ElementKind.INTERFACE) {
            "Actor annotation can only be used on interfaces"
        }
        return visitType(element, context).toWriter()
    }

    final override fun visitType(typeElement: TypeElement, context: GenerationContext): Bundle {
        val actorInterface = typeElement.asType().asTypeName()

        val newClassName = ClassName.bestGuess(proxyName(actorInterface.toString()))
        val parameterizedProxy = getProxyBaseClass(typeElement, context.typeUtil).parameterizedBy(actorInterface)

        val delegateParam = ParameterSpec.builder("delegate", actorInterface).build()
        val configParam = ParameterSpec.builder("config", ProxyConfiguration::class.asTypeName()).build()

        val ctr = FunSpec
                .constructorBuilder()
                .addParameter(delegateParam)
                .addParameter(configParam)
                .build()

        val classBuilder = TypeSpec.classBuilder(newClassName)
                .addAnnotation(context.generationAnnotation())
                .primaryConstructor(ctr)
                .superclass(parameterizedProxy)
                .addSuperclassConstructorParameter(delegateParam.name)
                .addSuperclassConstructorParameter(configParam.name)
                .addSuperinterface(actorInterface)

        val signaturesName = ClassName.bestGuess("${newClassName.canonicalName}Signatures")
        val functions = buildFunctions(typeElement, FunctionBuildContext(signaturesName), context)
        classBuilder.addFunctions(functions.map { it.funSpec })
        classBuilder.addProperty(buildHandlerMap(functions, signaturesName))

        return Bundle(listOf(
                TypeResult(newClassName, classBuilder.build()),
                buildSignatureObject(signaturesName, functions)
        ))
    }

    private fun buildSignatureObject(signaturesName: ClassName, functions: List<MethodResult>): TypeResult {
        val builder = functions.asSequence()
                .fold(TypeSpec.objectBuilder(signaturesName)) { acc, methodResult ->
                    val propBuilder = PropertySpec
                            .builder(methodResult.funSignature.symbolName, String::class, KModifier.CONST)
                            .initializer("%S", methodResult.funSignature.value)
                    acc.addProperty(propBuilder.build())
                }
        return TypeResult(signaturesName, builder.build())
    }

    abstract fun buildFunctions(typeElement: TypeElement, functionBuildContext: FunctionBuildContext, context: GenerationContext): List<MethodResult>

    private fun buildHandlerMap(methods: List<MethodResult>, signaturesName: ClassName): PropertySpec {
        val initializer = CodeBlock.builder().addStatement("%T()", HandlerBuilder::class)
        methods.forEach { method ->
            val invoke = if (method.funSpec.returnType == UNIT) {
                "unitFunction"
            } else {
                "valueFunction"
            }
            initializer.beginControlFlow(".%L(%T.`%L`) { params ->", invoke, signaturesName, method.funSignature.symbolName)
                    .addStatement("delegate.%L(", method.funSpec.name)
            method.funSpec.parameters.forEachIndexed { idx, param ->
                val comma = if (idx == 0) "" else ","
                val isArray = param.type.isArray()
                val isNullable = param.type.isNullable
                when {
                    isArray && isNullable -> initializer.add("%L params.getArrayNullable(%S, %T::class)", comma, param.name, param.type)
                    isArray && !isNullable -> initializer.add("%L params.getArray(%S, %T::class)", comma, param.name, param.type)
                    !isArray && isNullable -> initializer.add("%L params.getObjectNullable(%S)", comma, param.name)
                    !isArray && !isNullable -> initializer.add("%L params.getObject(%S)", comma, param.name)
                }
            }

            initializer.add(")").endControlFlow()
        }

        val parameterizedMap = MAP.parameterizedBy(STRING, MessageHandler::class.asClassName())
        return PropertySpec.builder("handlerMap", parameterizedMap)
                .addModifiers(KModifier.OVERRIDE, KModifier.FINAL)
                .initializer(initializer.addStatement(".build()").build())
                .build()
    }

    private fun getProxyBaseClass(e: TypeElement, typeUtil: TypeUtil): ClassName {
        val annotationValue = e.annotationMirrors.asSequence()
                .filter { it.annotationType.toString() == Actor::class.qualifiedName }
                .flatMap { annotation ->
                    typeUtil.getElementValuesWithDefaults(annotation).filterKeys { key ->
                        key.simpleName.contentEquals(Actor::value.name)
                    }.values.asSequence()
                }.first()


        val value = annotationValue.value as TypeMirror
        require(typeUtil.isValidProxy(value)) { "Type $value is not a valid proxy" }

        return ClassName.bestGuess(value.toString())
    }

    private fun TypeName.isArray(): Boolean {
        return this.toString().startsWith(ARRAY.canonicalName) ||
                this == BYTE_ARRAY ||
                this == BOOLEAN_ARRAY ||
                this == INT_ARRAY ||
                this == SHORT_ARRAY ||
                this == FLOAT_ARRAY ||
                this == LONG_ARRAY ||
                this == DOUBLE_ARRAY
    }
}
