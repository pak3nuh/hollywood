package pt.pak3nuh.hollywood.system.actor.message

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pt.pak3nuh.hollywood.actor.message.BooleanParameter
import pt.pak3nuh.hollywood.actor.message.ByteParameter
import pt.pak3nuh.hollywood.actor.message.DoubleParameter
import pt.pak3nuh.hollywood.actor.message.FloatParameter
import pt.pak3nuh.hollywood.actor.message.IntParameter
import pt.pak3nuh.hollywood.actor.message.LongParameter
import pt.pak3nuh.hollywood.actor.message.ReferenceParameter
import pt.pak3nuh.hollywood.actor.message.ShortParameter

internal class MessageBuilderImplTest {

    private val builder = MessageBuilderImpl()

    @Test
    internal fun `should create messages with empty parameters`() {
        val message = builder.build("id")
        assertThat(message.functionId).isEqualTo("id:")
        assertThat(message.parameters).isEmpty()
    }

    @Test
    internal fun `should retain parameter order`() {
        val message = builder.parameters {
            param("p1", "reference")
            param("p2", 1.toByte())
            param("p3", true)
            param("p4", 2.toShort())
            param("p5", 3)
            param("p6", 4L)
            param("p7", 5F)
            param("p8", 6.0)
        }.build("id")

        assertThat(message.functionId).isEqualTo("id:Lkotlin.String;B;Z;S;I;J;F;D")
        assertThat(message.parameters).containsExactly(
                ReferenceParameter("p1", "reference"),
                ByteParameter("p2", 1),
                BooleanParameter("p3", true),
                ShortParameter("p4", 2),
                IntParameter("p5", 3),
                LongParameter("p6",4),
                FloatParameter("p7", 5F),
                DoubleParameter("p8", 6.0)
        )
    }

    @Test
    internal fun `shouldn't allow parameters with the same name`() {
        assertThrows<IllegalArgumentException> {
            builder.parameters {
                param("p1", 1)
                param("p1", 6)
            }
        }
    }

    @Test
    internal fun `should concatenate parameters on multiple calls`() {
        builder.parameters {
            param("p1", "p1")
        }
        builder.parameters {
            param("p2", "p2")
        }
        val message = builder.build("id")
        assertThat(message.parameters).containsExactly(
                ReferenceParameter("p1", "p1"),
                ReferenceParameter("p2", "p2")
        )
    }

}
