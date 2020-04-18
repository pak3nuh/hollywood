package pt.pak3nuh.hollywood.processor.generator.util

import pt.pak3nuh.hollywood.processor.generator.metadata.type.MetaFun
import pt.pak3nuh.hollywood.processor.generator.metadata.type.MetaType
import pt.pak3nuh.hollywood.processor.generator.mirror.TypeUtil
import javax.lang.model.element.VariableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import javax.lang.model.type.WildcardType

interface TypeChecker {
    fun checkNotActor(variableType: TypeMirror)
    fun checkNotActor(type: MetaType)

    /**
     * @return The return type
     */
    fun checkIsSuspend(parameters: Iterable<VariableElement>, returnType: TypeMirror): TypeMirror
    /**
     * @return The return type
     */
    fun checkIsSuspend(metadata: MetaFun): MetaType
}

@Suppress("FunctionName")
fun TypeChecker(typeUtil: TypeUtil): TypeChecker = TypeCheckerImpl(typeUtil)

private const val NOT_SUSPENDABLE = "Function not suspendable"
private const val NOT_ACTOR = "Actors aren't allowed on parameters nor return types"

private class TypeCheckerImpl(
        private val typeUtil: TypeUtil
): TypeChecker {

    override fun checkNotActor(variableType: TypeMirror) {
        check(!typeUtil.isActor(variableType)) { NOT_ACTOR }
    }

    override fun checkNotActor(type: MetaType) {
        val typeName = type.name
        val element = typeUtil.getElement(typeName)
        val isActor = if (null == element) {
            // unknown element, most likely a flexible kotlin type (platform)
            false
        } else {
            typeUtil.isActor(element.asType())
        }
        check(!isActor) { NOT_ACTOR }
    }

    override fun checkIsSuspend(parameters: Iterable<VariableElement>, returnType: TypeMirror): TypeMirror {
        // something like nullable Continuation<? super type>
        val continuationParameter = parameters.asSequence()
                .mapIndexed { idx, p -> p.asType() to idx }
                .sortedByDescending {
                    // usually is the last parameter
                    it.second
                }
                .firstOrNull { typeUtil.isAssignableCoroutine(it.first) }
                ?.first

        checkNotNull(continuationParameter) { "Suspending functions must have a Continuation parameter" }
        check(typeUtil.isAssignable(returnType, typeUtil.coroutineJvmReturnType)) { NOT_SUSPENDABLE }

        val asDeclared = continuationParameter as DeclaredType
        val firstArgument = asDeclared.typeArguments.first()
        val argumentWildcard = firstArgument as WildcardType

        val superBound: TypeMirror? = argumentWildcard.superBound
        return superBound ?: typeUtil.nothingType
    }

    override fun checkIsSuspend(metadata: MetaFun): MetaType {
        check(metadata.isSuspend) { NOT_SUSPENDABLE }
        return metadata.returnType
    }
}
