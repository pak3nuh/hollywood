package pt.pak3nuh.hollywood.processor.generator.metadata

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import pt.pak3nuh.hollywood.processor.generator.MethodResult
import pt.pak3nuh.hollywood.processor.generator.MethodResult.FunSignature
import pt.pak3nuh.hollywood.processor.generator.context.GenerationContext
import pt.pak3nuh.hollywood.processor.generator.kpoet.accept
import pt.pak3nuh.hollywood.processor.generator.metadata.type.MetaClass
import pt.pak3nuh.hollywood.processor.generator.metadata.type.MetaFun
import pt.pak3nuh.hollywood.processor.generator.metadata.type.MetaParameter
import pt.pak3nuh.hollywood.processor.generator.metadata.type.WellKnownTypes
import pt.pak3nuh.hollywood.processor.generator.metadata.type.visitor.ClassLiteralVisitor
import pt.pak3nuh.hollywood.processor.generator.mirror.FunctionBuildContext
import pt.pak3nuh.hollywood.processor.generator.mirror.ProxyClassGenerator
import pt.pak3nuh.hollywood.processor.generator.util.FunctionSignatureBuilder
import pt.pak3nuh.hollywood.processor.generator.util.Logger
import pt.pak3nuh.hollywood.processor.generator.util.TypeChecker
import javax.lang.model.element.TypeElement


class KotlinProxyGenerator(
        private val typeChecker: TypeChecker
) : ProxyClassGenerator() {

    override fun buildFunctions(typeElement: TypeElement, functionBuildContext: FunctionBuildContext, context: GenerationContext): List<MethodResult> {
        val classMetadata = requireNotNull(context[MetaClass]) { "Kotlin class metadata is required" }
        return buildFunctions(classMetadata, functionBuildContext, context.logger)
    }

    private fun buildFunctions(metadata: MetaClass, functionContext: FunctionBuildContext, logger: Logger): List<MethodResult> {
        logger.logDebug("Generating functions of class ${metadata.name}")
        return metadata.functions.map { buildFunction(it, functionContext.signatureType, logger) }
    }

    private fun buildFunction(metadata: MetaFun, signaturesClassName: ClassName, logger: Logger): MethodResult {
        logger.logDebug("Generating function ${metadata.name}")
        val returnType = typeChecker.checkIsSuspend(metadata)
        typeChecker.checkNotActor(returnType)

        val parameters = metadata.parameters
                .onEach { typeChecker.checkNotActor(it.type) }
                .map { ParameterSpec.builder(it.name, it.type.asTypeName()).build() }
                .toList()

        val funSignature = buildSignature(metadata)
        val builder = FunSpec.builder(metadata.name)
                .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
                .returns(returnType.asTypeName())
                .addParameters(parameters)
                .addCode(buildDelegateCall(metadata.parameters, funSignature, signaturesClassName))

        return MethodResult(builder.build(), funSignature)
    }

    private fun buildSignature(metadata: MetaFun): FunSignature {
        val builder = FunctionSignatureBuilder()
        metadata.parameters.forEach {
            when (it) {
                WellKnownTypes.boolean -> builder.addBoolean()
                WellKnownTypes.byte -> builder.addByte()
                WellKnownTypes.short -> builder.addShort()
                WellKnownTypes.int -> builder.addInt()
                WellKnownTypes.float -> builder.addFloat()
                WellKnownTypes.long -> builder.addLong()
                WellKnownTypes.double -> builder.addDouble()
                else -> builder.addReference(it.type.asTypeName())
            }
        }
        return builder.build(metadata.name)
    }

    private fun buildDelegateCall(parameterSpecs: List<MetaParameter>, funSignature: FunSignature, signaturesClassName: ClassName): CodeBlock {
        val builder = CodeBlock.builder().add("return ")

        builder.beginControlFlow("sendAndAwait")

        builder.beginControlFlow("parameters")
        parameterSpecs.forEach {
            val literal = ClassLiteralVisitor().apply(it.type.asTypeName()::accept).result
            builder.addStatement("param(%S, %T::class, %L)", it.name, literal, it.name)
        }
        builder.endControlFlow()
        builder.addStatement("build(%T.`%L`)", signaturesClassName, funSignature.symbolName)
        builder.endControlFlow()
        return builder.build()
    }
}
