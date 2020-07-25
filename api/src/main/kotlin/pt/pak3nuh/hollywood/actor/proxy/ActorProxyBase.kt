package pt.pak3nuh.hollywood.actor.proxy

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import pt.pak3nuh.hollywood.actor.message.BooleanParameter
import pt.pak3nuh.hollywood.actor.message.ByteParameter
import pt.pak3nuh.hollywood.actor.message.DoubleParameter
import pt.pak3nuh.hollywood.actor.message.ExceptionResponse
import pt.pak3nuh.hollywood.actor.message.ExceptionReturn
import pt.pak3nuh.hollywood.actor.message.FloatParameter
import pt.pak3nuh.hollywood.actor.message.IntParameter
import pt.pak3nuh.hollywood.actor.message.LongParameter
import pt.pak3nuh.hollywood.actor.message.Message
import pt.pak3nuh.hollywood.actor.message.MessageBuilder
import pt.pak3nuh.hollywood.actor.message.Parameter
import pt.pak3nuh.hollywood.actor.message.ReferenceParameter
import pt.pak3nuh.hollywood.actor.message.Response
import pt.pak3nuh.hollywood.actor.message.ReturnType
import pt.pak3nuh.hollywood.actor.message.ShortParameter
import pt.pak3nuh.hollywood.actor.message.UnitResponse
import pt.pak3nuh.hollywood.actor.message.ValueResponse
import pt.pak3nuh.hollywood.actor.message.ValueReturn
import kotlin.coroutines.coroutineContext
import kotlin.reflect.KClass

/**
 * Base class for generated actor proxies.
 *
 * Any custom proxy that is expected to be plugged in into the code generator, should expose the same public interface.
 * Consider extending this class for increased compatibility with the code generator.
 */
abstract class ActorProxyBase<T>(override val delegate: T, private val configuration: ProxyConfiguration)
    : ActorProxy<T> {

    private val serializer = configuration.serializer
    private val deserializer = configuration.deserializer

    final override val actorId: String
        get() = configuration.actorId

    protected abstract val handlerMap: Map<String, MessageHandler>

    protected open suspend fun <T> sendAndAwait(block: MessageBuilder.() -> Message): T {
        return dispatchAndAwait(block(configuration.newMessageBuilder()))
    }

    private suspend fun <T> dispatchAndAwait(message: Message): T {
        return cast(dispatch(message))
    }

    private suspend fun dispatch(message: Message): Any? {
        val packedMessage = serializer.serialize(message)
        val currentJob = requireNotNull(coroutineContext[Job])
        val result = CompletableDeferred<ByteArray>(parent = currentJob)

        // simulated mailbox
        // actual implementation should use the actorSystem to send and receive the results
        configuration.scope.launch {
            val response = try {
                onMessage(packedMessage)
            } catch (e: Exception) {
                ExceptionResponse(e)
            }
            val asBytes = serializer.serialize(response)
            result.complete(asBytes)
        }

        val responseBytes = result.await()
        return onResponse(responseBytes)
    }

    private fun onResponse(responseBytes: ByteArray): Any? {
        val response = deserializer.asResponse(responseBytes)
        return parse(response)
    }

    private fun parse(response: Response): Any? {
        return when (response.returnType) {
            ReturnType.EXCEPTION -> {
                val exceptionReturn = response.returnValue as ExceptionReturn
                throw ProxyResponseException(exceptionReturn.message, exceptionReturn.stackTrace)
            }
            ReturnType.VALUE -> (response.returnValue as ValueReturn).value
            ReturnType.UNIT -> Unit
        }
    }

    protected open suspend fun onMessage(packedMessage: ByteArray): Response {
        val message = deserializer.asMessage(packedMessage)
        val params = MsgParamsImpl(message)
        return onMessage(message.functionId, params, this::raiseError)
    }

    private suspend fun onMessage(functionId: String, params: MsgParams, err: (String) -> Nothing): Response {
        val handler: MessageHandler? = handlerMap[functionId]
        if (handler == null) {
            err("Unknown function id $functionId")
        } else {
            return handler.handler(params)
        }
    }

    private fun raiseError(msg: String): Nothing {
        throw ProxyRequestException(msg)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <Y> cast(value: Any?): Y = value as Y

}

interface MessageHandler {
    val functionId: String
    val handler: suspend (MsgParams) -> Response
}

class HandlerBuilder {

    private val handlerList = mutableListOf<MessageHandler>()

    fun <T> valueFunction(functionId: String, handler: suspend (MsgParams) -> T): HandlerBuilder {
        val handlerDecorator = Handler(functionId) {
            ValueResponse(handler(it))
        }
        handlerList.add(handlerDecorator)
        return this
    }

    fun unitFunction(functionId: String, handler: suspend (MsgParams) -> Unit): HandlerBuilder {
        val handlerDecorator = Handler(functionId) {
            handler(it)
            UnitResponse()
        }
        handlerList.add(handlerDecorator)
        return this
    }

    fun build(): Map<String, MessageHandler> {
        return handlerList.associateBy(MessageHandler::functionId)
    }

    private data class Handler(
            override val functionId: String,
            override val handler: suspend (MsgParams) -> Response
    ) : MessageHandler
}

interface MsgParams {
    // todo can be improved to avoid boxing and unboxing
    fun <T> getObject(name: String): T
    fun <T> getObjectNullable(name: String): T?
    fun <T : Any> getArray(name: String, kClass: KClass<T>): T
    fun <T : Any> getArrayNullable(name: String, kClass: KClass<T>): T?
}

private class MsgParamsImpl(private val message: Message) : MsgParams {

    override fun <T> getObject(name: String): T = getObjectNullable(name) ?: error("Parameter $name can't be null")

    @Suppress("UNCHECKED_CAST")
    override fun <T> getObjectNullable(name: String): T? {
        return when (val param = getParam(name)) {
            is ReferenceParameter -> param.value
            is BooleanParameter -> param.value
            is ByteParameter -> param.value
            is ShortParameter -> param.value
            is IntParameter -> param.value
            is LongParameter -> param.value
            is FloatParameter -> param.value
            is DoubleParameter -> param.value
        } as T?
    }

    override fun <T : Any> getArray(name: String, kClass: KClass<T>): T {
        return getObject(name)
    }

    override fun <T : Any> getArrayNullable(name: String, kClass: KClass<T>): T? {
        return getObjectNullable(name)
    }

    private fun getParam(name: String): Parameter {
        val nullParam = message.parameters.firstOrNull { it.name == name }
        return requireNotNull(nullParam) { "Parameter with name $name not found" }
    }
}

class ProxyRequestException(message: String?) : RuntimeException(message)
class ProxyResponseException(message: String?, stackTrace: List<StackTraceElement>?) : RuntimeException(message)
