package pt.pak3nuh.hollywood.processor.generator

import pt.pak3nuh.hollywood.processor.Actor
import pt.pak3nuh.hollywood.processor.generator.context.GenerationContext
import pt.pak3nuh.hollywood.processor.generator.util.Logger
import java.nio.file.Paths
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

class GeneratorFacade : AbstractProcessor() {

    private lateinit var logger: Logger

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        if (annotations.isEmpty()) {
            return false
        }

        check(annotations.size == 1) { "Only expect one annotation, got $annotations" }
        val actorAnnotation: TypeElement = annotations.first()

        val destinationFolder: String = processingEnv.options["kapt.kotlin.generated"]
                ?: error("Generated source folder is mandatory")

        generateFiles(roundEnv.getElementsAnnotatedWith(actorAnnotation), destinationFolder)

        return true
    }

    private fun generateFiles(annotatedElements: Set<Element>, destinationFolder: String) {
        val ctx = GenerationContext(logger)
        val generators = sequenceOf<FileGenerator>(
                ActorProxyGenerator(processingEnv.elementUtils),
                ActorFactoryGenerator()
        )
        annotatedElements.asSequence()
                .filterIsInstance<TypeElement>()
                .onEach {
                    logger.logInfo("Discovered element for processing $it")
                }.flatMap { element ->
                    generators.map { generator ->
                        generator.generate(element, ctx)
                    }
                }.forEach {
                    logger.logInfo("Writing source file $it")
                    it.writeTo(Paths.get(destinationFolder))
                }
    }

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        logger = Logger(processingEnv.messager)
    }

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.RELEASE_6

    override fun getSupportedAnnotationTypes(): MutableSet<String> = mutableSetOf(Actor::class.qualifiedName!!)
}
