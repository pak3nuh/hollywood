package pt.pak3nuh.hollywood.system.actor.message.serializer

import assertk.assertThat
import assertk.assertions.isFalse
import org.junit.jupiter.api.Test
import pt.pak3nuh.hollywood.actor.message.Message
import pt.pak3nuh.hollywood.system.actor.message.serializer.externalizable.ExternalizableSerDes

internal class ExternalizableSerDesTest: BaseSerDesTest() {

    override val supportsUnitResponse: Boolean = true
    override val supportsExceptionResponse: Boolean = true
    override val serdes = ExternalizableSerDes()

    override fun providesNonNullResponseValue(): Any {
        return RecursiveExternalizable(ExternalizableString("other string value"))
    }

    override fun provideNonNullReferenceMessage(): Message {
        return messageBuilder.parameters {
            param("string", ExternalizableString::class, ExternalizableString("some string value"))
            param("null", ExternalizableString::class, null)
            param("deep", RecursiveExternalizable::class, RecursiveExternalizable(ExternalizableString("other string value")))
        }.build("references")
    }

    @Test
    internal fun `should not support non externalizable messages`() {
        val message = messageBuilder.parameters {
            param("non externalizable", String::class, "some string")
        }.build("non externalizable parameters")

        assertThat(serdes.supports(message)).isFalse()
    }

}