package pt.pak3nuh.hollywood.system.actor

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import pt.pak3nuh.hollywood.actor.proxy.ActorScope
import pt.pak3nuh.hollywood.actor.proxy.ContextMap
import pt.pak3nuh.hollywood.actor.proxy.MdcContext
import java.util.concurrent.Executors
import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

class SystemScope(threadCount: Int) : ActorScope {

    override val mainJob = SupervisorJob()
    private val exHandler = CancellationExceptionHandler(mainJob)
    private val dispatcher = MdcDispatcher(threadCount)

    override val coroutineContext: CoroutineContext = mainJob + exHandler + dispatcher + MdcContext("NA")

}

private class CancellationExceptionHandler(private val parentJob: Job) : CoroutineExceptionHandler {
    override val key: CoroutineContext.Key<CoroutineExceptionHandler> = CoroutineExceptionHandler

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        parentJob.cancel(CancellationException(null, exception))
    }
}

private class MdcDispatcher(threadCount: Int) : ContinuationInterceptor {

    private val dispatcher = Executors.newFixedThreadPool(threadCount).asCoroutineDispatcher()
    override val key = object : CoroutineContext.Key<MdcDispatcher> {}

    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> {
        return dispatcher.interceptContinuation(MdcContinuation(continuation, MdcContext.getCopyOfContextMap()))
    }
}

private class MdcContinuation<T>(val continuation: Continuation<T>, val oldContext: ContextMap) : Continuation<T> {
    override val context: CoroutineContext
        get() = continuation.context

    override fun resumeWith(result: Result<T>) {
        try {
            MdcContext.setContextMap(continuation.context[MdcContext.Key]?.contextMap)
            continuation.resumeWith(result)
        } finally {
            MdcContext.setContextMap(oldContext)
        }
    }
}
