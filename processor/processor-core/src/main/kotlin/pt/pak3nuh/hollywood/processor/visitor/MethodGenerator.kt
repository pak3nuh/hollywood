package pt.pak3nuh.hollywood.processor.visitor

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeName
import pt.pak3nuh.hollywood.processor.generator.MethodResult
import pt.pak3nuh.hollywood.processor.generator.Result
import pt.pak3nuh.hollywood.processor.generator.context.GenerationContext
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Name
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import javax.lang.model.type.WildcardType

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
                .filterIsInstance<TypeElement>()
                .filter { !context.isAssignableCoroutine(it.asType()) }
                .map { toSpec(it.simpleName, context.asKotlinTypeName(it)) }

        val builder = FunSpec.builder(methodName)
                .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
                .returns(context.asKotlinTypeName(returnType))
                .addParameters(parameterSpecs.asIterable())
                .addStatement(buildDelegateCall(context, methodName, returnType, parameterSpecs))

        return MethodResult(builder.build())
    }

    private fun buildDelegateCall(context: GenerationContext, methodName: String, returnType: TypeMirror, parameterSpecs: Sequence<ParameterSpec>): String {
        val parametersAsString = parameterSpecs.map { it.name }.joinToString(", ")
        fun buildCall() = """execCall {
                                    |delegate.$methodName($parametersAsString)
                            |}""".trimMargin()
        return if (context.isAssignable(returnType, context.unitType)) {
            buildCall()
        } else {
            "return ${buildCall()}"
        }
    }

    private fun toSpec(name: Name, type: TypeName): ParameterSpec {
        // todo translate java to kotlin collections
        return ParameterSpec.builder(name.toString(), type)
                .build()
    }

    private fun checkIsSuspend(parameters: Iterable<VariableElement>, returnType: TypeMirror, context: GenerationContext): TypeMirror {
        // something like nullable Continuation<? super type>
        val continuationParameter = parameters.asSequence()
                .mapIndexed { idx, p ->
                    p.asType() to idx
                }.sortedByDescending {
            // usually is the last parameter
            it.second
        }.firstOrNull {
            context.isAssignableCoroutine(it.first)
        }?.first

        checkNotNull(continuationParameter) { "Suspending functions must have a Continuation parameter" }
        check(context.isAssignable(returnType, context.coroutineJvmReturnType)) { "Return type is not valid for suspending functions" }

        val asDeclared = continuationParameter as DeclaredType
        val firstArgument = asDeclared.typeArguments.first()
        val argumentWildcard = firstArgument as WildcardType

        return argumentWildcard.superBound
    }
}
