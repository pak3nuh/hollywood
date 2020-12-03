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
import pt.pak3nuh.hollywood.actor.message.mapStackElement
import pt.pak3nuh.hollywood.system.actor.message.MessageBuilderImpl
import pt.pak3nuh.hollywood.system.actor.message.serializer.externalizable.ExternalizableSerDes
import pt.pak3nuh.hollywood.system.actor.message.serializer.kotlin.KSerDesHolder
import pt.pak3nuh.hollywood.system.actor.message.serializer.kotlin.KotlinSerDes
import pt.pak3nuh.hollywood.system.actor.message.serializer.kotlin.TestKSerDesProvider
import java.io.ByteArrayInputStream
import java.time.Instant

internal class LateBindStrategyTest {

    private val strategy = LateBindStrategy(DefaultSerializer(), ExternalizableSerDes(), KotlinSerDes(setOf(TestKSerDesProvider())))
    private val messageBuilder = MessageBuilderImpl(emptySet())

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
                    transform { it.stackTrace }.isEqualTo(exception.stackTrace?.map(::mapStackElement))
                }
    }

    @Test
    internal fun `should select externalizable serializer`() {
        val message = messageBuilder.parameters {
            param("extref", ExternalizableString::class, ExternalizableString("some value"))
        }.build("message")
        val bytes = strategy.serialize(message)
        val stream = ByteArrayInputStream(bytes)
        val type = stream.read()
        assertThat(type).isEqualTo(LateBindStrategy.StrategyType.Externalizable.ordinal)
    }

    @Test
    internal fun `should select kotlin serializer`() {
        val message = messageBuilder.parameters {
            param("extref", KSerDesHolder::class, KSerDesHolder("some value"))
        }.build("message")
        val bytes = strategy.serialize(message)
        val stream = ByteArrayInputStream(bytes)
        val type = stream.read()
        assertThat(type).isEqualTo(LateBindStrategy.StrategyType.Kotlin.ordinal)
    }

    @Test
    internal fun `should select default serializer`() {
        val message = messageBuilder.parameters {
            param("extref", ExternalizableString::class, ExternalizableString("some value"))
            param("ref", String::class, "some value")
        }.build("message")
        val bytes = strategy.serialize(message)
        val stream = ByteArrayInputStream(bytes)
        val type = stream.read()
        assertThat(type).isEqualTo(LateBindStrategy.StrategyType.Default.ordinal)
    }

    data class Holder(val instant: Instant)
}
