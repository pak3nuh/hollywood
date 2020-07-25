package pt.pak3nuh.hollywood.sandbox.serializer

import pt.pak3nuh.hollywood.processor.api.SerializerData
import pt.pak3nuh.hollywood.processor.api.SerializerProvider

class KotlinSerializerExtension: SerializerProvider {
    override fun getAllSerializers(): Set<SerializerData> {
        return emptySet()
    }
}
