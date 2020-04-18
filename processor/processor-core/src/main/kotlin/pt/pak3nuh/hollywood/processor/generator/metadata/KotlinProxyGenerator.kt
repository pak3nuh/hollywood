package pt.pak3nuh.hollywood.processor.generator.metadata

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import pt.pak3nuh.hollywood.processor.generator.MethodResult
import pt.pak3nuh.hollywood.processor.generator.context.GenerationContext
import pt.pak3nuh.hollywood.processor.generator.metadata.type.MetaClass
import pt.pak3nuh.hollywood.processor.generator.metadata.type.MetaFun
import pt.pak3nuh.hollywood.processor.generator.metadata.type.MetaType
import pt.pak3nuh.hollywood.processor.generator.metadata.type.WellKnownTypes
import pt.pak3nuh.hollywood.processor.generator.mirror.ProxyClassGenerator
import pt.pak3nuh.hollywood.processor.generator.util.Logger
import pt.pak3nuh.hollywood.processor.generator.util.TypeChecker
import javax.lang.model.element.TypeElement


class KotlinProxyGenerator(
        private val typeChecker: TypeChecker
) : ProxyClassGenerator() {

    override fun buildFunctions(typeElement: TypeElement, context: GenerationContext): List<MethodResult> {
        val classMetadata = requireNotNull(context[MetaClass]) { "Kotlin class metadata is required" }
        return buildFunctions(classMetadata, context.logger)
    }

    private fun buildFunctions(metadata: MetaClass, logger: Logger): List<MethodResult> {
        logger.logDebug("Generating functions of class ${metadata.name}")
        return metadata.functions.map { buildFunction(it, logger) }
    }

    private fun buildFunction(metadata: MetaFun, logger: Logger): MethodResult {
        logger.logDebug("Generating function ${metadata.name}")
        val returnType = typeChecker.checkIsSuspend(metadata)
        typeChecker.checkNotActor(returnType)

        val parameters = metadata.parameters
                .onEach { typeChecker.checkNotActor(it.type) }
                .map { ParameterSpec.builder(it.name ,it.type.asTypeName()).build() }
                .toList()

        val builder = FunSpec.builder(metadata.name)
                .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
                .returns(returnType.asTypeName())
                .addParameters(parameters)
                .addCode(buildDelegateCall(metadata.name, returnType, parameters))

        return MethodResult(builder.build())
    }

    private fun buildDelegateCall(methodName: String, returnType: MetaType, parameterSpecs: List<ParameterSpec>): CodeBlock {
        val parametersAsString = parameterSpecs.joinToString(", ") { it.name }
        val hasReturnStatement = returnType != WellKnownTypes.unitType
        val builder = if (hasReturnStatement) {
            CodeBlock.builder().add("return ")
        } else {
            CodeBlock.builder()
        }

        return builder.beginControlFlow("execCall")
                .indent().addStatement("delegate.%L(%L)", methodName, parametersAsString)
                .unindent().endControlFlow()
                .build()
    }
}
