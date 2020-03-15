package pt.pak3nuh.hollywood.processor.generator

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import pt.pak3nuh.hollywood.processor.generator.context.GenerationContext
import pt.pak3nuh.hollywood.processor.visitor.MethodElementVisitor
import javax.lang.model.element.ElementVisitor
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import javax.lang.model.type.WildcardType

internal typealias MethodVisitor = ElementVisitor<Result, GenerationContext>

class MethodGenerator : MethodElementVisitor() {

    override fun visitExecutable(method: ExecutableElement, context: GenerationContext): Result {
        return buildMethodResult(context, method)
    }

    private fun buildMethodResult(context: GenerationContext, method: ExecutableElement): MethodResult {
        val methodName = method.simpleName.toString()
        context.logger.logInfo("Building method $methodName")
        context.logger.logDebug("Checking is suspend")
        val returnType = checkIsSuspend(method.parameters, method.returnType, context)

        val parameterSpecs = method.parameters.asSequence()
                .filter { !context.typeUtil.isAssignableCoroutine(it.asType()) }
                .map {
                    ParameterSpec.builder(it.simpleName.toString(), context.typeUtil.convert(it))
                            .build()
                }
                .toList()

        // todo doc generic methods not supported by design
        val builder = FunSpec.builder(methodName)
                .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
                .returns(context.typeUtil.convert(returnType))
                .addParameters(parameterSpecs)
                .addCode(buildDelegateCall(context, methodName, returnType, parameterSpecs))

        return MethodResult(builder.build())
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

    private fun checkIsSuspend(parameters: Iterable<VariableElement>, returnType: TypeMirror, context: GenerationContext): TypeMirror {
        // something like nullable Continuation<? super type>
        val continuationParameter = parameters.asSequence()
                .mapIndexed { idx, p -> p.asType() to idx }
                .sortedByDescending {
                    // usually is the last parameter
                    it.second
                }
                .firstOrNull { context.typeUtil.isAssignableCoroutine(it.first) }
                ?.first

        checkNotNull(continuationParameter) { "Suspending functions must have a Continuation parameter" }
        check(context.typeUtil.isAssignable(returnType, context.typeUtil.coroutineJvmReturnType)) { "Return type is not valid for suspending functions" }

        val asDeclared = continuationParameter as DeclaredType
        val firstArgument = asDeclared.typeArguments.first()
        val argumentWildcard = firstArgument as WildcardType

        return argumentWildcard.superBound
    }
}
