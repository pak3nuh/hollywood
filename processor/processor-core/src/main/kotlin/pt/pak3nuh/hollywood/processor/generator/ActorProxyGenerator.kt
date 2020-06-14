package pt.pak3nuh.hollywood.processor.generator

import pt.pak3nuh.hollywood.processor.generator.context.GenerationContext
import pt.pak3nuh.hollywood.processor.generator.metadata.type.MetaClass
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement

class ActorProxyGenerator(
        private val metadataExtractor: (TypeElement) -> MetaClass?,
        private val kotlinMetadataGenerator: FileGenerator
) : FileGenerator {

    override fun generate(element: TypeElement, context: GenerationContext): FileWriter {
        require(element.kind == ElementKind.INTERFACE) {
            "Actor annotation can only be used on interfaces"
        }

        context[MetaClass] = metadataExtractor(element) ?: error("Couldn't load kotlin metadata for element $element")
        val sourceFile = kotlinMetadataGenerator.generate(element, context)
        context.remove(MetaClass)

        return sourceFile
    }
}
