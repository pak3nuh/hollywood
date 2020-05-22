package pt.pak3nuh.hollywood.system.actor

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import pt.pak3nuh.hollywood.actor.proxy.ActorScope
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

// todo docs
class SystemScope(threadCount: Int) : ActorScope {

    val mainJob = SupervisorJob()
    private val exHandler = CancellationExceptionHandler(mainJob)
    private val dispatcher = Executors.newFixedThreadPool(threadCount).asCoroutineDispatcher()

    override val coroutineContext: CoroutineContext = mainJob + exHandler + dispatcher

}

private class CancellationExceptionHandler(private val parentJob: Job) : CoroutineExceptionHandler {
    override val key: CoroutineContext.Key<CoroutineExceptionHandler> = CoroutineExceptionHandler

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        parentJob.cancel(CancellationException(null, exception))
    }
}