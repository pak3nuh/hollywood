package pt.pak3nuh.hollywood.processor.generator.mirror

import pt.pak3nuh.hollywood.processor.generator.MethodResult
import pt.pak3nuh.hollywood.processor.generator.context.GenerationContext
import javax.lang.model.element.TypeElement

interface FunctionGenerator {
    fun buildFunctions(typeElement: TypeElement, functionBuildContext: FunctionBuildContext, context: GenerationContext): List<MethodResult>
}