package pt.pak3nuh.hollywood.processor.generator.mirror

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import pt.pak3nuh.hollywood.processor.generator.MethodResult
import pt.pak3nuh.hollywood.processor.generator.Result
import pt.pak3nuh.hollywood.processor.generator.context.GenerationContext
import pt.pak3nuh.hollywood.processor.generator.mirror.visitor.MethodElementVisitor
import pt.pak3nuh.hollywood.processor.generator.util.TypeChecker
import javax.lang.model.element.ElementVisitor
import javax.lang.model.element.ExecutableElement
import javax.lang.model.type.TypeMirror

internal typealias MethodVisitor = ElementVisitor<Result, GenerationContext>

class MethodGenerator(
        private val typeChecker: TypeChecker
) : MethodElementVisitor() {

    override fun visitExecutable(method: ExecutableElement, context: GenerationContext): Result {
        return buildMethodResult(context, method)
    }

    private fun buildMethodResult(context: GenerationContext, method: ExecutableElement): MethodResult {
        val methodName = method.simpleName.toString()
        context.logger.logInfo("Building method $methodName")
        context.logger.logDebug("Checking is suspend")
        val returnType = typeChecker.checkIsSuspend(method.parameters, method.returnType)
        typeChecker.checkNotActor(returnType)

        val parameterSpecs = method.parameters.asSequence()
                .filter { !context.typeUtil.isAssignableCoroutine(it.asType()) }
                .onEach {
                    val variableType = it.asType()
                    typeChecker.checkNotActor(variableType)
                }
                .map {
                    ParameterSpec.builder(it.simpleName.toString(), context.typeUtil.convert(it))
                            .build()
                }
                .toList()

        val builder = FunSpec.builder(methodName)
                .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
                .returns(context.typeUtil.convert(returnType))
                .addParameters(parameterSpecs)
                .addCode(buildDelegateCall(context, methodName, returnType, parameterSpecs))

        // TODO
        return MethodResult(builder.build(), error("TODO"))
    }

    private fun buildDelegateCall(context: GenerationContext, methodName: String, returnType: TypeMirror, parameterSpecs: List<ParameterSpec>): CodeBlock {
        val parametersAsString = parameterSpecs.joinToString(", ") { it.name }
        val hasReturnStatement = !context.typeUtil.isAssignable(returnType, context.typeUtil.unitType)
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
