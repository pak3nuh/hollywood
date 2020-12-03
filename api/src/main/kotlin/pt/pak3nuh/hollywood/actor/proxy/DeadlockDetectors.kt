package pt.pak3nuh.hollywood.actor.proxy

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import pt.pak3nuh.hollywood.actor.message.Message
import pt.pak3nuh.hollywood.actor.message.Response
import kotlin.coroutines.CoroutineContext

private val logger: Logger = LoggerFactory.getLogger(StatefulDeadlockDetector::class.java)

/**
 * Designed to be called with different coroutines when sending and when receiving messages.
 * Deadlocks occur when one coroutine is blocked waiting for another, so it is ensured that
 * the current stack doesn't change until another received message is processed.
 */
class StatefulDeadlockDetector(private val identifier: String) {

    private var currentTrace: Set<String> = emptySet()

    suspend fun <T> onSendMessage(block: suspend (Set<String>) -> T): T {
        val traceCopy = currentTrace
        logger.debug("Checking deadlocks for trace {}", traceCopy)
        if (traceCopy.contains(identifier)) {
            logger.error("Deadlock on identifier {} for trace {}", identifier, traceCopy)
            throw ProxyRequestException("Deadlock detected with identifier $identifier.")
        }
        logger.trace("Adding identifier {} to current message", identifier)
        val newTrace = traceCopy + identifier
        return block(newTrace)
    }

    suspend fun onReceiveMessage(message: Message, block: suspend () -> Response): Response {
        logger.debug("Restoring trace {}", message.trace)
        currentTrace = message.trace
        return block().also {
            currentTrace = emptySet()
        }
    }

}

private class MessageTrace(val trace: Set<String>) : CoroutineContext.Element {
    override val key = KEY

    companion object KEY : CoroutineContext.Key<MessageTrace>
}
