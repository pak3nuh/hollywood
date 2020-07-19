package pt.pak3nuh.hollywood.processor.generator.actor

import pt.pak3nuh.hollywood.processor.Actor
import pt.pak3nuh.hollywood.processor.generator.BaseProcessor
import pt.pak3nuh.hollywood.processor.generator.FileGenerator
import pt.pak3nuh.hollywood.processor.generator.context.GenerationContextImpl
import pt.pak3nuh.hollywood.processor.generator.metadata.KotlinMetadataExtractor
import pt.pak3nuh.hollywood.processor.generator.metadata.MetadataFunctionGenerator
import pt.pak3nuh.hollywood.processor.generator.mirror.ProxyFilesGenerator
import pt.pak3nuh.hollywood.processor.generator.mirror.TypeConverter
import pt.pak3nuh.hollywood.processor.generator.mirror.TypeUtilImpl
import pt.pak3nuh.hollywood.processor.generator.util.TypeChecker
import java.nio.file.Paths
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

class ActorProcessor : BaseProcessor(Actor::class.qualifiedName!!) {

    override fun processElements(annotatedElements: Set<Element>, generatedFolder: String) {
        val typeUtil = TypeUtilImpl(logger, processingEnv.typeUtils, processingEnv.elementUtils, TypeConverter())
        val ctx = GenerationContextImpl(logger, typeUtil)
        val typeChecker = TypeChecker(typeUtil)
        val proxyGenerator = ProxyFilesGenerator(MetadataFunctionGenerator(typeChecker))
        val kotlinMetadataExtractor = KotlinMetadataExtractor(typeUtil.metadataType, logger)
        val generators = sequenceOf<FileGenerator>(
                ActorProxyGenerator(kotlinMetadataExtractor::extract, proxyGenerator),
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
                    it.writeTo(Paths.get(generatedFolder))
                }
    }

}
