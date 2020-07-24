package pt.pak3nuh.hollywood.system.actor.message.serializer.kotlin

import kotlinx.serialization.Serializable
import pt.pak3nuh.hollywood.actor.message.Message
import pt.pak3nuh.hollywood.processor.api.SerializerData
import pt.pak3nuh.hollywood.processor.api.SerializerProvider
import pt.pak3nuh.hollywood.system.actor.message.serializer.BaseSerDesTest

internal class KotlinSerDesTest: BaseSerDesTest() {

    override val supportsUnitResponse: Boolean = false
    override val supportsExceptionResponse: Boolean = false
    override val serdes = KotlinSerDes(setOf(BuiltinKotlinSerializers(), TestKSerDesProvider()))

    override fun providesNonNullResponseValue(): Any {
        return "some supported value"
    }

    override fun provideNonNullReferenceMessage(): Message {
        return messageBuilder.parameters {
            param("string", String::class, "some string value")
            param("deep", KSerDesHolder::class, KSerDesHolder("other string value"))
        }.build("references")
    }
}

internal class TestKSerDesProvider: SerializerProvider {
    override fun getAllSerializers(): Set<SerializerData> {
        return setOf(
                SerializerData(KSerDesHolder::class, KSerDesHolder.serializer())
        )
    }
}

@Serializable
internal data class KSerDesHolder(val value: String)