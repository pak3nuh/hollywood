package pt.pakenuh.hollywood.sandbox.coroutine

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import pt.pakenuh.hollywood.sandbox.Loggers
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

// todo move scope to actor system
// prepare semantics of the concurrency model
object TestScope : CoroutineScope {
    val job = SupervisorJob()
    private val exHandler = CancellationExceptionHandler(job)
    private val dispatcher = Executors.newFixedThreadPool(1).asCoroutineDispatcher() // todo increase thread number

    override val coroutineContext: CoroutineContext = job + exHandler + dispatcher

}

private class CancellationExceptionHandler(private val parentJob: Job) : CoroutineExceptionHandler {
    override val key: CoroutineContext.Key<CoroutineExceptionHandler> = CoroutineExceptionHandler

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        LOGGER.severe("Error running coroutine, canceling scope")
        LOGGER.info(exception.toString())
        parentJob.cancel(CancellationException(null, exception))
    }

    private companion object {
        val LOGGER = Loggers.getLogger<CancellationExceptionHandler>()
    }
}
