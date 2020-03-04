package pt.pak3nuh.hollywood.processor.visitor

import pt.pak3nuh.hollywood.processor.generator.NoOpResult
import pt.pak3nuh.hollywood.processor.generator.Result
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeParameterElement
import javax.lang.model.element.VariableElement
import javax.lang.model.util.AbstractElementVisitor6

abstract class TypeElementVisitor: AbstractElementVisitor6<Result, Unit>() {

    final override fun visitTypeParameter(e: TypeParameterElement?, p: Unit?): Result = NoOpResult

    override fun visitExecutable(e: ExecutableElement, p: Unit): Result = NoOpResult

    final override fun visitVariable(e: VariableElement?, p: Unit?): Result = NoOpResult

    final override fun visitPackage(e: PackageElement?, p: Unit?): Result = NoOpResult
}