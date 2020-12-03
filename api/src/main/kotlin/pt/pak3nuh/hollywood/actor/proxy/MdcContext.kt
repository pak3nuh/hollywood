package pt.pak3nuh.hollywood.actor.proxy

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import org.slf4j.MDC
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

typealias ContextMap = Map<String, String>?

/**
 * [MDC] context stored for each coroutine.
 *
 * It maintains a copy of the context map for it to be restored to MDC when the coroutine is
 * attached.
 * All MDC operations made to this context are synchronized with the thread's MDC context.
 */
class MdcContext(actorId: String) : CoroutineContext.Element {

    override val key: CoroutineContext.Key<*> = Key

    /**
     * Map of values associated with a coroutine context.
     */
    val contextMap: MutableMap<String, String> = HashMap()

    init {
        contextMap[MdcKeys.ACTOR_ID_KEY] = actorId
    }

    object Key : CoroutineContext.Key<MdcContext>

    fun put(key: String, value: String) {
        MDC.put(key, value)
        contextMap[key] = value
    }

    fun remove(key: String) {
        MDC.remove(key)
        contextMap.remove(key)
    }

    companion object {

        suspend fun put(key: String, value: String) {
            coroutineContext[Key]?.put(key, value)
        }

        suspend fun remove(key: String) {
            coroutineContext[Key]?.remove(key)
        }

        fun getCopyOfContextMap(): ContextMap = MDC.getCopyOfContextMap()

        fun setContextMap(map: ContextMap) {
            if (map != null) {
                MDC.setContextMap(map)
            } else {
                MDC.clear()
            }
        }
    }

}
