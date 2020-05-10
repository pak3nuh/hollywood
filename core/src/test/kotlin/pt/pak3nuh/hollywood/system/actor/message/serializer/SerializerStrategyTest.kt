package pt.pak3nuh.hollywood.system.actor.message.serializer

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import org.junit.jupiter.api.Test
import pt.pak3nuh.hollywood.actor.message.ExceptionResponse
import pt.pak3nuh.hollywood.actor.message.ExceptionReturn

import pt.pak3nuh.hollywood.actor.message.ReturnType
import pt.pak3nuh.hollywood.actor.message.UnitResponse
import pt.pak3nuh.hollywood.actor.message.UnitReturn
import pt.pak3nuh.hollywood.actor.message.ValueResponse
import pt.pak3nuh.hollywood.actor.message.ValueReturn
import java.time.Instant

internal class SerializerStrategyTest {

    private val strategy = SerializerStrategy(DefaultSerializer())

    @Test
    fun unitResponse() {
        val bytes = strategy.serialize(UnitResponse())
        val response = strategy.asResponse(bytes)
        assertThat(response.returnType).isEqualTo(ReturnType.UNIT)
        assertThat(response.returnValue).isInstanceOf(UnitReturn::class)
    }

    @Test
    internal fun nullValueResponse() {
        val bytes = strategy.serialize(ValueResponse(null))
        val response = strategy.asResponse(bytes)
        assertThat(response.returnType).isEqualTo(ReturnType.VALUE)
        assertThat(response.returnValue).isInstanceOf(ValueReturn::class)
                .transform { it.value }.isNull()
    }

    @Test
    internal fun valueResponse() {
        val instant = Instant.now()
        val bytes = strategy.serialize(ValueResponse(Holder(instant)))
        val response = strategy.asResponse(bytes)
        assertThat(response.returnType).isEqualTo(ReturnType.VALUE)
        assertThat(response.returnValue).isInstanceOf(ValueReturn::class)
                .transform { it.value }.isNotNull()
                .isInstanceOf(Holder::class).transform { it.instant }.isEqualTo(instant)
    }

    @Test
    internal fun exceptionResponse() {
        val exception = Exception("message")
        val bytes = strategy.serialize(ExceptionResponse(exception))
        val response = strategy.asResponse(bytes)
        assertThat(response.returnType).isEqualTo(ReturnType.EXCEPTION)
        assertThat(response.returnValue).isInstanceOf(ExceptionReturn::class)
                .all {
                    transform { it.message }.isEqualTo(exception.message)
                    transform { it.stackTrace }.isEqualTo(exception.stackTrace?.toList())
                }
    }

    data class Holder(val instant: Instant)
}
