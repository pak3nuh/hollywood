package pt.pak3nuh.hollywood.processor.generator

import pt.pak3nuh.hollywood.processor.generator.util.Logger
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

abstract class BaseProcessor(private val annotationName: String): AbstractProcessor() {

    protected lateinit var logger: Logger

    final override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        if (annotations.isEmpty()) {
            return false
        }

        check(annotations.size == 1) { "Only expect one annotation $annotationName, got $annotations" }
        val targetAnnotation: TypeElement = annotations.first()

        val destinationFolder: String = processingEnv.options["kapt.kotlin.generated"]
                ?: error("Generated source folder is mandatory")

        try {
            processElements(roundEnv.getElementsAnnotatedWith(targetAnnotation), destinationFolder)
            return true
        } catch (ex: Exception) {
            logger.logError(ex)
            throw ex
        }
    }

    protected abstract fun processElements(annotatedElements: Set<Element>, generatedFolder: String)

    final override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.RELEASE_8

    final override fun getSupportedAnnotationTypes(): Set<String> = setOf(annotationName)

    final override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        logger = Logger(processingEnv.messager)
    }
}