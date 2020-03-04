package pt.pak3nuh.hollywood.processor.generator

import pt.pak3nuh.hollywood.processor.generator.context.GenerationContext
import pt.pak3nuh.hollywood.processor.generator.context.Logger
import java.nio.file.Paths
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes("pt.pak3nuh.hollywood.processor.Actor")
class GeneratorFacade : AbstractProcessor() {
    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        if (annotations.isEmpty()) {
            return false
        }

        check(annotations.size == 1) { "Only expect one annotation, got $annotations" }
        val actorAnnotation: TypeElement = annotations.first()

        val destinationFolder: String = processingEnv.options["kapt.kotlin.generated"]
                ?: throw IllegalStateException("Generated source folder is mandatory")

        val logger = Logger(processingEnv.messager)
        val ctx = GenerationContext(logger)
        roundEnv.getElementsAnnotatedWith(actorAnnotation)
                .asSequence()
                .flatMap {
                    logger.logInfo("Discovered element for processing $it")
                    sequenceOf<Generator>(
                            ActorProxyGenerator(it, ctx),
                            ActorFactoryGenerator(it, ctx)
                    )
                }.map {
                    it.generate()
                }.forEach {
                    it.writeTo(Paths.get(destinationFolder))
                }

        return true
    }

}