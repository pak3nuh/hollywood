package pt.pak3nuh.hollywood.processor.generator

import pt.pak3nuh.hollywood.processor.Actor
import pt.pak3nuh.hollywood.processor.generator.context.GenerationContextImpl
import pt.pak3nuh.hollywood.processor.generator.metadata.KotlinMetadataExtractor
import pt.pak3nuh.hollywood.processor.generator.metadata.KotlinProxyGenerator
import pt.pak3nuh.hollywood.processor.generator.mirror.JavaProxyGenerator
import pt.pak3nuh.hollywood.processor.generator.mirror.MethodGenerator
import pt.pak3nuh.hollywood.processor.generator.mirror.TypeConverter
import pt.pak3nuh.hollywood.processor.generator.mirror.TypeUtilImpl
import pt.pak3nuh.hollywood.processor.generator.util.Logger
import pt.pak3nuh.hollywood.processor.generator.util.TypeChecker
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

        try {
            generateFiles(roundEnv.getElementsAnnotatedWith(actorAnnotation), destinationFolder)
            return true
        } catch (ex: Exception) {
            logger.logError(ex)
            throw ex
        }
    }

    private fun generateFiles(annotatedElements: Set<Element>, destinationFolder: String) {
        val typeUtil = TypeUtilImpl(logger, processingEnv.typeUtils, processingEnv.elementUtils, TypeConverter())
        val ctx = GenerationContextImpl(logger, typeUtil, isMetadataEnabled())
        val typeChecker = TypeChecker(typeUtil)
        val javaGenerator = JavaProxyGenerator(MethodGenerator(typeChecker))
        val kotlinMethodGenerator = KotlinProxyGenerator(typeChecker)
        val kotlinMetadataExtractor = KotlinMetadataExtractor(typeUtil.metadataType, logger)
        val generators = sequenceOf<FileGenerator>(
                ActorProxyGenerator(kotlinMetadataExtractor::extract, javaGenerator, kotlinMethodGenerator),
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

    private fun isMetadataEnabled(): Boolean {
        val property: String? = System.getProperty("hollywood.processor.disable-kotlin-metadata")
        return property?.toBoolean()?.not() ?: true
    }

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        logger = Logger(processingEnv.messager)
    }

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.RELEASE_8

    override fun getSupportedAnnotationTypes(): Set<String> = setOf(Actor::class.qualifiedName!!)
}
