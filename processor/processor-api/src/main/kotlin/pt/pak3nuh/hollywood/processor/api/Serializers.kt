package pt.pak3nuh.hollywood.processor.api

import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

/**
 * SPI to discover available serializers.
 */
// decouples code generator from serializer usage
interface SerializerProvider {
    fun getAllSerializers(): Set<SerializerData>

    fun <T: Any> getSerializer(kClass: KClass<T>): KSerializer<T>? {
        return getAllSerializers().filter { it.kClass == kClass }
                .map { it.serializer as KSerializer<T> }
                .firstOrNull()
    }
}

data class SerializerData(val kClass: KClass<*>, val serializer: KSerializer<*>)