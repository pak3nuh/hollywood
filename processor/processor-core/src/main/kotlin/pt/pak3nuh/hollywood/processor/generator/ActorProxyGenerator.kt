package pt.pak3nuh.hollywood.processor.generator

import pt.pak3nuh.hollywood.processor.generator.context.GenerationContext
import pt.pak3nuh.hollywood.processor.generator.metadata.type.MetaClass
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement

class ActorProxyGenerator(
        private val metadataExtractor: (TypeElement) -> MetaClass?,
        private val javacGenerator: FileGenerator,
        private val kotlinMetadataGenerator: FileGenerator
) : FileGenerator {

    override fun generate(element: TypeElement, context: GenerationContext): SourceFile {
        require(element.kind == ElementKind.INTERFACE) {
            "Actor annotation can only be used on interfaces"
        }

        val kotlinMetadata = metadataExtractor(element)
        val sourceFile = if (kotlinMetadata == null) {
            context.logger.logInfo("Kotlin metadata not available for type $element")
            javacGenerator.generate(element, context)
        } else {
            context[MetaClass] = kotlinMetadata
            context.logger.logInfo("Using Kotlin metadata for type $element")
            kotlinMetadataGenerator.generate(element, context)
        }

        context.remove(MetaClass)
        return sourceFile
    }
}
