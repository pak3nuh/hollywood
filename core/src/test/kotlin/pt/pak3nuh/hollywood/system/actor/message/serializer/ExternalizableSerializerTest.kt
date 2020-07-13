package pt.pak3nuh.hollywood.system.actor.message.serializer

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isGreaterThan
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test
import pt.pak3nuh.hollywood.actor.message.Message
import pt.pak3nuh.hollywood.actor.message.Parameter
import pt.pak3nuh.hollywood.system.actor.message.MessageBuilderImpl

internal class ExternalizableSerializerTest {

    val serializer = ExternalizableSerializer()
    val deserializer = ExternalizableDeserializer()
    val messageBuilder = MessageBuilderImpl()

    @Test
    internal fun `should serde messages with only primitives`() {
        val message = messageBuilder.parameters {
            param("boolean", false)
            param("byte", 1.toByte())
            param("short", 2.toShort())
            param("int", 3)
            param("long", 4L)
            param("float", 5f)
            param("double", 6.0)
        }.build("primitives")

        val clone = clone(message)

        assertEquivalent(message, clone)
    }

    private fun clone(message: Message): Message {
        assertThat(serializer.supports(message)).isTrue()

        val bytes = serializer.serialize(message)
        assertThat(bytes.size).isGreaterThan(0)
        return deserializer.asMessage(bytes)
    }

    @Test
    internal fun `should serde with no parameters`() {
        val message = messageBuilder.build("no parameters")
        val clone = clone(message)
        assertEquivalent(message, clone)
    }

    @Test
    internal fun `should serde null references`() {
        val message = messageBuilder.parameters {
            param("first", String::class, null)
            param("second", Int::class, null)
        }.build("null references")

        val clone = clone(message)

        assertEquivalent(message, clone)
    }

    @Test
    internal fun `should serde references`() {
        val message = messageBuilder.parameters {
            param("string", ExternalizableString::class, ExternalizableString("some string value"))
            param("null", ExternalizableString::class, null)
            param("deep", RecursiveExternalizable::class, RecursiveExternalizable(ExternalizableString("other string value")))
        }.build("references")

        val clone = clone(message)

        assertEquivalent(message, clone)
    }

    @Test
    internal fun `should not support non externalizable messages`() {
        val message = messageBuilder.parameters {
            param("non externalizable", String::class, "some string")
        }.build("non externalizable parameters")

        assertThat(serializer.supports(message)).isFalse()
    }

    private fun assertEquivalent(original: Message, clone: Message) {
        assertThat(original.functionId).isEqualTo(clone.functionId)
        assertThat(original.parameters).hasSize(clone.parameters.size)
        for(i in original.parameters.indices) {
            assertEquivalent(original.parameters[i], clone.parameters[i])
        }
    }

    private fun assertEquivalent(original: Parameter, clone: Parameter) {
        assertThat(original).isInstanceOf(clone::class)
        assertThat(original).isEqualTo(clone)
    }
}