package pt.pak3nuh.hollywood.system.actor.message.serializer

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotSameAs
import org.junit.jupiter.api.Test
import pt.pak3nuh.hollywood.actor.message.BooleanParameter
import pt.pak3nuh.hollywood.actor.message.ByteParameter
import pt.pak3nuh.hollywood.actor.message.DoubleParameter
import pt.pak3nuh.hollywood.actor.message.FloatParameter
import pt.pak3nuh.hollywood.actor.message.IntParameter
import pt.pak3nuh.hollywood.actor.message.LongParameter
import pt.pak3nuh.hollywood.actor.message.ReferenceParameter
import pt.pak3nuh.hollywood.actor.message.ShortParameter

internal class DefaultSerializerTest {

    private val serializer = DefaultSerializer()

    @Test
    internal fun `should serde empty message`() {
        val testMessage = TestMessage("some id", emptyList())
        val copy = serde(testMessage)
        assertEquivalent(testMessage, copy)
    }

    @Test
    internal fun `should serde message with parameters`() {
        val testMessage = TestMessage("id",
                listOf(
                        BooleanParameter("p1", true),
                        ByteParameter("p2", 5),
                        ShortParameter("p3", 10),
                        IntParameter("p4", 20),
                        LongParameter("p5", 30),
                        FloatParameter("p6", 40F),
                        DoubleParameter("p7", 50.0),
                        ReferenceParameter("string", "some string"),
                        ReferenceParameter("final args ctr only", FinalClass(1)),
                        ReferenceParameter("open args ctr only", OpenClass(1)),
                        ReferenceParameter("open args ctr only", NoArgsClass().apply { data = 5 })
                )
        )
        val copy = serde(testMessage)
        assertEquivalent(testMessage, copy)
    }

    @Test
    internal fun `should serde arrays`() {
        val expected = intArrayOf(1, 2, 3)
        val testMessage = TestMessage("1", listOf(ReferenceParameter("open args ctr only", expected)))
        val copy = serde(testMessage)
        assertThat(copy.parameters[0])
                .isInstanceOf(ReferenceParameter::class)
                .transform { it.value!! }
                .isInstanceOf(IntArray::class)
                .containsExactly(*expected)
    }

    private fun assertEquivalent(testMessage: TestMessage, copy: TestMessage) {
        assertThat(testMessage).isNotSameAs(copy)
        assertThat(testMessage).isEqualTo(copy)
    }

    private fun serde(testMessage: TestMessage): TestMessage {
        val asBytes = serializer.serialize(testMessage)
        val moshiMessage = serializer.deserializeMessage(asBytes)
        return TestMessage(moshiMessage.functionId, moshiMessage.parameters)
    }
}

open class OpenClass(val data: Int) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OpenClass

        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        return data
    }
}
data class FinalClass(val data: Int)
class NoArgsClass {
    var data: Int? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NoArgsClass

        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        return data ?: 0
    }

}
