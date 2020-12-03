package pt.pak3nuh.hollywood.actor.proxy

import assertk.assertThat
import assertk.assertions.containsOnly
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import pt.pak3nuh.hollywood.actor.message.Message
import pt.pak3nuh.hollywood.actor.message.Parameter
import pt.pak3nuh.hollywood.actor.message.UnitResponse

internal class CoroutineDeadlockDetectorTest {

    private val identifier = "1"
    private val deadlockDetector = StatefulDeadlockDetector(identifier)

    @Test
    internal fun `should not trigger deadlock on non existing trace`() {
        runBlocking {
            deadlockDetector.onSendMessage {
                assertThat(it).containsOnly(identifier)
            }
        }
    }

    @Test
    internal fun `should not trigger deadlock on empty trace`() {
        runBlocking {
            deadlockDetector.onReceiveMessage(MsgStub(emptySet())) {
                deadlockDetector.onSendMessage {
                    assertThat(it).containsOnly(identifier)
                    UnitResponse()
                }
            }
        }
    }

    @Test
    internal fun `should not trigger deadlock on non recurring trace`() {
        runBlocking {
            deadlockDetector.onReceiveMessage(MsgStub(setOf("2", "3"))) {
                deadlockDetector.onSendMessage {
                    assertThat(it).containsOnly("2", "3", identifier)
                    UnitResponse()
                }
            }
        }
    }

    @Test
    internal fun `should trigger deadlock on recurring trace`() {
        assertThrows(ProxyRequestException::class.java) {
            runBlocking {
                deadlockDetector.onReceiveMessage(MsgStub(setOf("2", "3", identifier, "5"))) {
                    deadlockDetector.onSendMessage {}
                    UnitResponse()
                }
            }
        }
    }

    private class MsgStub(override val trace: Set<String>): Message {
        override val functionId: String
            get() = TODO("Not yet implemented")
        override val parameters: List<Parameter>
            get() = TODO("Not yet implemented")
    }
}
