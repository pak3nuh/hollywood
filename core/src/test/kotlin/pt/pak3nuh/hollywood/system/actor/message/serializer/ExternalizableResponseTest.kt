package pt.pak3nuh.hollywood.system.actor.message.serializer

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test
import pt.pak3nuh.hollywood.actor.message.ExceptionResponse
import pt.pak3nuh.hollywood.actor.message.Response
import pt.pak3nuh.hollywood.actor.message.UnitResponse
import pt.pak3nuh.hollywood.actor.message.ValueResponse
import pt.pak3nuh.hollywood.system.actor.message.serializer.externalizable.ExternalizableSerDes
import java.io.ByteArrayInputStream


internal class ExternalizableResponseTest{

    val serdes = ExternalizableSerDes()

    @Test
    internal fun `should serde unit responses`() {
        val response = UnitResponse()

        val clone = clone(response)

        assertEquivalent(response, clone)
    }

    private fun assertEquivalent(original: Response, clone: Response) {
        assertThat(original.returnType).isEqualTo(clone.returnType)
        assertThat(original.returnValue).isEqualTo(clone.returnValue)
    }

    private fun clone(response: Response): Response {
        assertThat(serdes.supports(response)).isTrue()
        val bytes = serdes.serialize(response)
        return serdes.deserializeResponse(ByteArrayInputStream(bytes))
    }

    @Test
    internal fun `should serde null values`() {
        val response = ValueResponse(null)
        val clone = clone(response)
        assertEquivalent(response, clone)
    }

    @Test
    internal fun `should not support non externalizable values`() {
        val response = ValueResponse("")
        assertThat(serdes.supports(response)).isFalse()
    }

    @Test
    internal fun `should serde externalizable values`() {
        val response = ValueResponse(RecursiveExternalizable(ExternalizableString("some string")))
        val clone = clone(response)
        assertEquivalent(response, clone)
    }

    @Test
    internal fun `should serde exceptions without message`() {
        val response = ExceptionResponse(RuntimeException())
        val clone = clone(response)
        assertEquivalent(response, clone)
    }

    @Test
    internal fun `should serde exceptions with message`() {
        val response = ExceptionResponse(IllegalStateException("some invalid state"))
        val clone = clone(response)
        assertEquivalent(response, clone)
    }
}