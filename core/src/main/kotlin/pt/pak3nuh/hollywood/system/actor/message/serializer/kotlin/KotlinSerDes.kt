package pt.pak3nuh.hollywood.system.actor.message.serializer.kotlin

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.cbor.Cbor
import pt.pak3nuh.hollywood.actor.message.BooleanParameter
import pt.pak3nuh.hollywood.actor.message.ByteParameter
import pt.pak3nuh.hollywood.actor.message.DoubleParameter
import pt.pak3nuh.hollywood.actor.message.FloatParameter
import pt.pak3nuh.hollywood.actor.message.IntParameter
import pt.pak3nuh.hollywood.actor.message.KClassMetadata
import pt.pak3nuh.hollywood.actor.message.LongParameter
import pt.pak3nuh.hollywood.actor.message.Message
import pt.pak3nuh.hollywood.actor.message.Parameter
import pt.pak3nuh.hollywood.actor.message.ReferenceParameter
import pt.pak3nuh.hollywood.actor.message.Response
import pt.pak3nuh.hollywood.actor.message.ReturnType
import pt.pak3nuh.hollywood.actor.message.ShortParameter
import pt.pak3nuh.hollywood.actor.message.ValueResponse
import pt.pak3nuh.hollywood.actor.message.ValueReturn
import pt.pak3nuh.hollywood.processor.api.GeneratedSerializerProvider
import pt.pak3nuh.hollywood.processor.api.SerializerData
import pt.pak3nuh.hollywood.processor.api.SerializerProvider
import pt.pak3nuh.hollywood.system.actor.message.MessageImpl
import pt.pak3nuh.hollywood.system.actor.message.serializer.InternalSerDes
import pt.pak3nuh.hollywood.system.actor.message.serializer.kotlin.KSerDesParameter.Type
import pt.pak3nuh.hollywood.util.log.getLogger
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.OutputStream
import java.util.*
import kotlin.reflect.KClass

internal class KotlinSerDes(private val serializerProviders: Set<SerializerProvider>) : InternalSerDes {

    private val supportedClasses: Set<KClass<*>> by lazy(this::buildSupportedSet)

    private fun buildSupportedSet(): Set<KClass<*>> {
        return serializerProviders.asSequence().flatMap { provider ->
            logger.info("Adding Serializers from $provider")
            provider.getAllSerializers().asSequence().map {
                logger.debug("Adding serializer data $it")
                it.kClass
            }
        }.toSet()
    }

    override fun serialize(message: Message, stream: OutputStream) =
            ObjectOutputStream(stream).use { serialize(message, it) }

    private fun serialize(message: Message, stream: ObjectOutputStream) {
        logger.debug("Serializing message {}", message)
        val paramList = message.parameters.map { param ->
            logger.trace("Converting parameter {}", param)
            when (param) {
                is ReferenceParameter -> {
                    val kClass = requireNotNull(param.metadata as KClassMetadata?).kClass
                    logger.trace("Getting serializer for KClass {}", kClass)
                    val serializer: KSerializer<Any> = requireSerializer(kClass)
                    val reference = param.value?.let {
                        KSerDesValue(kClass.java.name, Cbor.dump(serializer, it))
                    }
                    KSerDesParameter(Type.Ref, param.name, reference = reference)
                }
                is BooleanParameter -> KSerDesParameter(Type.Boolean, param.name, boolean = param.value)
                is ByteParameter -> KSerDesParameter(Type.Byte, param.name, byte = param.value)
                is ShortParameter -> KSerDesParameter(Type.Short, param.name, short = param.value)
                is IntParameter -> KSerDesParameter(Type.Int, param.name, int = param.value)
                is LongParameter -> KSerDesParameter(Type.Long, param.name, long = param.value)
                is FloatParameter -> KSerDesParameter(Type.Float, param.name, float = param.value)
                is DoubleParameter -> KSerDesParameter(Type.Double, param.name, double = param.value)
            }
        }

        val kotlinMessage = KSerDesMessage(message.functionId, paramList)
        logger.debug("Writing to stream {}", kotlinMessage)
        val serialized = Cbor.dump(KSerDesMessage.serializer(), kotlinMessage)
        stream.writeInt(serialized.size)
        stream.write(serialized)
        logger.debug("Message serialized")
    }

    private fun requireSerializer(kClass: KClass<*>): KSerializer<Any> {
        // todo try to reify class metadata
        logger.debug("Requiring serializer for KClass {}", kClass)
        return serializerProviders.asSequence().mapNotNull { it.getSerializer(kClass) }.first() as KSerializer<Any>
    }

    override fun serialize(response: Response, stream: OutputStream) =
            ObjectOutputStream(stream).use { serialize(response, it) }

    private fun serialize(response: Response, stream: ObjectOutputStream) {
        logger.debug("Serializing response {}", response)
        val kResponse: KSerDesResponse = when(response.returnType) {
            ReturnType.UNIT, ReturnType.EXCEPTION -> throw UnsupportedOperationException()
            ReturnType.VALUE -> {
                val value = (response.returnValue as ValueReturn).value
                logger.trace("Response value: {}", value)
                val kValue = value?.let {
                    val kClass = value::class
                    val serializer = requireSerializer(kClass)
                    KSerDesValue(kClass.java.name, Cbor.dump(serializer, it))
                }
                KSerDesResponse(kValue)
            }
        }

        logger.debug("Writing response to stream {}", kResponse)
        val bytes = Cbor.dump(KSerDesResponse.serializer(), kResponse)
        stream.writeInt(bytes.size)
        stream.write(bytes)
    }

    override fun supports(message: Message): Boolean {
        val result = message.parameters.asSequence()
                .all { param ->
                    when (param) {
                        is ReferenceParameter -> {
                            (param.metadata as? KClassMetadata)?.let { supportedClasses.contains(it.kClass) } ?: false
                        }
                        else -> true
                    }
                }
        logger.debug("Supports for message {} is {}", message, result)
        return result
    }

    override fun supports(response: Response): Boolean {
        val result = when (response.returnType) {
            ReturnType.UNIT, ReturnType.EXCEPTION -> false
            ReturnType.VALUE -> {
                val asValue = response.returnValue as ValueReturn
                asValue.value?.let { supportedClasses.contains(it::class) } ?: true
            }
        }
        logger.debug("Supports for response {} is {}", response, result)
        return result
    }

    override fun deserializeMessage(stream: InputStream): Message =
            ObjectInputStream(stream).use(this::deserializeMessage)

    private fun deserializeMessage(stream: ObjectInputStream): Message {
        logger.debug("Deserializing message")
        val size = stream.readInt()
        val buffer = ByteArray(size)
        stream.read(buffer)
        logger.trace("Loading message wrapper")
        val kotlinMessage = Cbor.load(KSerDesMessage.serializer(), buffer)
        logger.trace("Converting parameters")
        val params: List<Parameter> = kotlinMessage.parameters.map {
            logger.trace("Converting parameter {}", it)
            when (it.type) {
                Type.Boolean -> BooleanParameter(it.name, it.boolean)
                Type.Byte -> ByteParameter(it.name, it.byte)
                Type.Short -> ShortParameter(it.name, it.short)
                Type.Int -> IntParameter(it.name, it.int)
                Type.Long -> LongParameter(it.name, it.long)
                Type.Float -> FloatParameter(it.name, it.float)
                Type.Double -> DoubleParameter(it.name, it.double)
                Type.Ref -> {
                    it.reference?.let { ref ->
                        logger.debug("Loading parameter of type {}", ref.className)
                        val kClass = Class.forName(ref.className).kotlin
                        val value = Cbor.load(requireSerializer(kClass), ref.bytes)
                        ReferenceParameter(it.name, kClass, value)
                    } ?: ReferenceParameter(it.name, null, null)
                }
            }
        }
        logger.debug("Message {} loaded with parameters {}", kotlinMessage.functionId, params)
        return MessageImpl(kotlinMessage.functionId, params)
    }

    override fun deserializeResponse(stream: InputStream): Response =
            ObjectInputStream(stream).use(this::deserializeResponse)

    private fun deserializeResponse(stream: ObjectInputStream): Response {
        logger.debug("Deserializing response")
        val size = stream.readInt()
        val buffer = ByteArray(size)
        stream.read(buffer)
        logger.trace("Loading response wrapper")
        val kotlinResponse = Cbor.load(KSerDesResponse.serializer(), buffer)
        val value = kotlinResponse.value?.let {
            logger.debug("Loading response of type {}", it.className)
            val kClass = Class.forName(it.className).kotlin
            Cbor.load(requireSerializer(kClass), it.bytes)
        }
        logger.debug("Response loaded {}", value)
        return ValueResponse(value)
    }

    private companion object {
        val logger = getLogger<KotlinSerDes>()
    }
}


/**
 * Discovers existing [SerializerProvider] using the SPI and delegates method calls.
 */
class KotlinSerDesDiscovery: InternalSerDes {
    private val delegate: InternalSerDes by lazy(this::buildSerdes)

    private fun buildSerdes(): InternalSerDes {
        val manualProviders = ServiceLoader.load(SerializerProvider::class.java)
        val generatedProviders = ServiceLoader.load(GeneratedSerializerProvider::class.java)
        return KotlinSerDes(manualProviders.asSequence().plus(generatedProviders).toSet())
    }

    override fun serialize(message: Message, stream: OutputStream) = delegate.serialize(message, stream)

    override fun serialize(response: Response, stream: OutputStream) = delegate.serialize(response, stream)

    override fun supports(message: Message): Boolean = delegate.supports(message)

    override fun supports(response: Response): Boolean = delegate.supports(response)

    override fun deserializeMessage(stream: InputStream): Message = delegate.deserializeMessage(stream)

    override fun deserializeResponse(stream: InputStream): Response = delegate.deserializeResponse(stream)
}

/**
 * Built in serializers that ship with the library.
 */
class BuiltinKotlinSerializers : SerializerProvider {
    override fun getAllSerializers(): Set<SerializerData> = setOf(
            SerializerData(Boolean::class, Boolean.serializer()),
            SerializerData(Byte::class, Byte.serializer()),
            SerializerData(Short::class, Short.serializer()),
            SerializerData(Int::class, Int.serializer()),
            SerializerData(Long::class, Long.serializer()),
            SerializerData(Float::class, Float.serializer()),
            SerializerData(Double::class, Double.serializer()),
            SerializerData(String::class, String.serializer())
    )
}
