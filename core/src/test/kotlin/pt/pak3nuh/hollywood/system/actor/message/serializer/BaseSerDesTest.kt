package pt.pak3nuh.hollywood.system.actor.message.serializer

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isGreaterThan
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test
import pt.pak3nuh.hollywood.actor.message.ExceptionResponse
import pt.pak3nuh.hollywood.actor.message.Message
import pt.pak3nuh.hollywood.actor.message.Parameter
import pt.pak3nuh.hollywood.actor.message.Response
import pt.pak3nuh.hollywood.actor.message.UnitResponse
import pt.pak3nuh.hollywood.actor.message.ValueResponse
import pt.pak3nuh.hollywood.system.actor.message.MessageBuilderImpl
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

internal abstract class BaseSerDesTest {

    protected abstract val supportsUnitResponse: Boolean
    protected abstract val supportsExceptionResponse: Boolean
    protected abstract val serdes: InternalSerDes
    protected val messageBuilder = MessageBuilderImpl(emptySet())

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
        assertThat(serdes.supports(message)).isTrue()

        val stream = ByteArrayOutputStream()
        serdes.serialize(message, stream)
        val bytes = stream.toByteArray()
        assertThat(bytes.size).isGreaterThan(0)
        return serdes.deserializeMessage(ByteArrayInputStream(bytes))
    }

    private fun clone(response: Response): Response {
        assertThat(serdes.supports(response)).isTrue()

        val stream = ByteArrayOutputStream()
        serdes.serialize(response, stream)
        val bytes = stream.toByteArray()
        assertThat(bytes.size).isGreaterThan(0)
        return serdes.deserializeResponse(ByteArrayInputStream(bytes))
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

    internal abstract fun provideNonNullReferenceMessage(): Message

    @Test
    internal fun `should serde non null references`() {
        val message = provideNonNullReferenceMessage()

        val clone = clone(message)

        assertEquivalent(message, clone)
    }

    @Test
    internal fun `should serde null value response`() {
        val response = ValueResponse(null)
        val clone = clone(response)
        assertEquivalent(response, clone)
    }

    internal abstract fun providesNonNullResponseValue(): Any

    @Test
    internal fun `should serde non null value response`() {
        val response = ValueResponse(providesNonNullResponseValue())
        val clone = clone(response)
        assertEquivalent(response, clone)
    }

    @Test
    internal fun `should serdes unit response if supported`() {
        val response = UnitResponse()
        if (supportsUnitResponse) {
            val clone = clone(response)
            assertEquivalent(response, clone)
        } else {
            assertThat(serdes.supports(response)).isFalse()
        }
    }

    @Test
    internal fun `should serdes exception response if supported`() {
        val response = ExceptionResponse(UnsupportedOperationException())
        if (supportsExceptionResponse) {
            val clone = clone(response)
            assertEquivalent(response, clone)
        } else {
            assertThat(serdes.supports(response)).isFalse()
        }
    }

    private fun assertEquivalent(response: Response, clone: Response) {
        assertThat(response.returnType).isEqualTo(clone.returnType)
        assertThat(response.returnValue).isEqualTo(clone.returnValue)
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
