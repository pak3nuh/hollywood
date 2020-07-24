package pt.pak3nuh.hollywood.processor.generator.serializer

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import kotlinx.serialization.Serializable
import pt.pak3nuh.hollywood.processor.api.SerializerProvider
import pt.pak3nuh.hollywood.processor.generator.BaseProcessor
import java.nio.file.Files
import java.nio.file.Paths
import javax.lang.model.element.Element

const val SPI_IMPL_NAME = "pt.pak3nuh.hollywood.serializer.spi.generated.KotlinSerializationBridge"

/**
 * Generates the bridge that loads the serializers at runtime.
 */
class KotlinSerializerProcessor : BaseProcessor(Serializable::class.qualifiedName!!) {

    override fun processElements(annotatedElements: Set<Element>, generatedFolder: String) {
        // todo check if services are merged between generated folder and sources
        // kapt doesn't support multiple rounds of annotation processing, so no need to gather state
        generateServicesFile(generatedFolder)
        val className = ClassName.bestGuess(SPI_IMPL_NAME)
        val typeSpec = SerializationBridgeBuilder(className)
                .addBridges(annotatedElements)
                .build()
        FileSpec.builder(className.packageName, className.simpleName)
                .addType(typeSpec)
                .build()
                .writeTo(Paths.get(generatedFolder))
    }

    private fun generateServicesFile(generatedFolder: String) {
        val root = Paths.get(generatedFolder)
        val servicesFolder = root.resolve("META-INF/services")
        Files.deleteIfExists(servicesFolder)
        val directory = Files.createDirectories(servicesFolder)
        val services = Files.createFile(directory.resolve(SerializerProvider::class.qualifiedName!!))
        Files.write(services, SPI_IMPL_NAME.toByteArray())
    }

}
