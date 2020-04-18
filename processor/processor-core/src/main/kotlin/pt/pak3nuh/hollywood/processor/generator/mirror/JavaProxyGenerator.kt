package pt.pak3nuh.hollywood.processor.generator.mirror

import pt.pak3nuh.hollywood.processor.generator.MethodResult
import pt.pak3nuh.hollywood.processor.generator.context.GenerationContext
import javax.lang.model.element.TypeElement

class JavaProxyGenerator(
        private val methodGenerator: MethodVisitor
) : ProxyClassGenerator() {

    override fun buildFunctions(typeElement: TypeElement, context: GenerationContext): List<MethodResult> {
        return typeElement.enclosedElements.asSequence()
                .map { it.accept(methodGenerator, context) }
                .filterIsInstance<MethodResult>()
                .toList()
    }

}
