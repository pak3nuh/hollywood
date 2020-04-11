package pt.pak3nuh.hollywood.processor.generator.mirror.visitor

import pt.pak3nuh.hollywood.processor.generator.NoOpResult
import pt.pak3nuh.hollywood.processor.generator.Result
import pt.pak3nuh.hollywood.processor.generator.context.GenerationContext
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeParameterElement
import javax.lang.model.element.VariableElement
import javax.lang.model.util.AbstractElementVisitor8

abstract class TypeElementVisitor: AbstractElementVisitor8<Result, GenerationContext>() {

    final override fun visitTypeParameter(e: TypeParameterElement?, p: GenerationContext): Result = NoOpResult

    final override fun visitExecutable(e: ExecutableElement, p: GenerationContext): Result = NoOpResult

    final override fun visitVariable(e: VariableElement?, p: GenerationContext): Result = NoOpResult

    final override fun visitPackage(e: PackageElement?, p: GenerationContext): Result = NoOpResult
}
