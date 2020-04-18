package pt.pak3nuh.hollywood.processor.generator.mirror.visitor

import pt.pak3nuh.hollywood.processor.generator.NoOpResult
import pt.pak3nuh.hollywood.processor.generator.Result
import pt.pak3nuh.hollywood.processor.generator.context.GenerationContext
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.TypeParameterElement
import javax.lang.model.element.VariableElement
import javax.lang.model.util.AbstractElementVisitor8

abstract class MethodElementVisitor : AbstractElementVisitor8<Result, GenerationContext>() {

    override fun visitType(e: TypeElement?, p: GenerationContext?) = NoOpResult

    override fun visitTypeParameter(e: TypeParameterElement?, p: GenerationContext?) = NoOpResult

    override fun visitVariable(e: VariableElement?, p: GenerationContext?) = NoOpResult

    override fun visitPackage(e: PackageElement?, p: GenerationContext?) = NoOpResult
}
