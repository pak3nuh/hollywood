package pt.pak3nuh.hollywood.processor.generator

import pt.pak3nuh.hollywood.processor.generator.context.GenerationContext
import java.nio.file.Paths
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

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

        val ctx = GenerationContext()
        roundEnv.getElementsAnnotatedWith(actorAnnotation)
                .asSequence()
                .map {
                    logInfo("Discovered element for processing $it")
                    ActorProxyGenerator(it, ctx)
                }.map {
                    it.generate()
                }.forEach {
                    it.writeTo(Paths.get(destinationFolder))
                }

        return true
    }

    private fun logInfo(message: String) {
        processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, message)
    }

}