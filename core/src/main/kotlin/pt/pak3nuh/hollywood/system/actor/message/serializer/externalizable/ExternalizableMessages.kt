package pt.pak3nuh.hollywood.system.actor.message.serializer.externalizable

import org.slf4j.Logger
import pt.pak3nuh.hollywood.actor.message.BooleanParameter
import pt.pak3nuh.hollywood.actor.message.ByteParameter
import pt.pak3nuh.hollywood.actor.message.DoubleParameter
import pt.pak3nuh.hollywood.actor.message.ExceptionResponse
import pt.pak3nuh.hollywood.actor.message.ExceptionReturn
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
import pt.pak3nuh.hollywood.actor.message.StackElement
import pt.pak3nuh.hollywood.actor.message.UnitResponse
import pt.pak3nuh.hollywood.actor.message.ValueResponse
import pt.pak3nuh.hollywood.actor.message.ValueReturn
import pt.pak3nuh.hollywood.system.actor.message.MessageImpl
import pt.pak3nuh.hollywood.util.log.getLogger
import java.io.Externalizable
import java.io.ObjectInput
import java.io.ObjectOutput

class ExternalizableMessage() : Externalizable {

    lateinit var message: Message

    constructor(msg: Message) : this() {
        message = msg
    }

    constructor(input: ObjectInput) : this() {
        readExternal(input)
    }

    override fun readExternal(input: ObjectInput) {
        logger.debug("Start reading message")
        val functionId = input.readUTF()
        val parameterSize = input.readInt()
        logger.trace("ID: {}, params: {}", functionId, parameterSize)
        val paramList = (0 until parameterSize).map {
            val paramName = input.readUTF()
            val paramType = input.readByte()
            logger.trace("Parameter name: {}, type: {}", paramName, paramType)
            val param = when (ParamType.values()[paramType.toInt()]) {
                ParamType.Bool -> BooleanParameter(paramName, input.readBoolean())
                ParamType.Byte -> ByteParameter(paramName, input.readByte())
                ParamType.Short -> ShortParameter(paramName, input.readShort())
                ParamType.Int -> IntParameter(paramName, input.readInt())
                ParamType.Long -> LongParameter(paramName, input.readLong())
                ParamType.Float -> FloatParameter(paramName, input.readFloat())
                ParamType.Double -> DoubleParameter(paramName, input.readDouble())
                ParamType.Ref -> {
                    val instance = readExternalizableObject(input, logger)
                    if (instance == null) {
                        ReferenceParameter(paramName, null, null)
                    } else {
                        ReferenceParameter(paramName, instance::class, instance)
                    }
                }
            }
            logger.trace("Param: {}", param)
            param
        }

        val traceSize = input.readInt()
        logger.trace("Trace size: {}", traceSize)
        val trace = (0 until traceSize).map {
            input.readUTF()
        }.toSet()

        message = MessageImpl(functionId, paramList, trace)
        logger.debug("Finished reading message: {}", message)
    }

    override fun writeExternal(output: ObjectOutput) {
        output.writeUTF(message.functionId)
        output.writeInt(message.parameters.size)
        message.parameters.forEach {
            output.writeUTF(it.name)
            when (it) {
                is BooleanParameter -> {
                    output.writeByte(ParamType.Bool.ordinal)
                    output.writeBoolean(it.value)
                }
                is ByteParameter -> {
                    output.writeByte(ParamType.Byte.ordinal)
                    output.writeByte(it.value.toInt())
                }
                is ShortParameter -> {
                    output.writeByte(ParamType.Short.ordinal)
                    output.writeShort(it.value.toInt())
                }
                is IntParameter -> {
                    output.writeByte(ParamType.Int.ordinal)
                    output.writeInt(it.value)
                }
                is LongParameter -> {
                    output.writeByte(ParamType.Long.ordinal)
                    output.writeLong(it.value)
                }
                is FloatParameter -> {
                    output.writeByte(ParamType.Float.ordinal)
                    output.writeFloat(it.value)
                }
                is DoubleParameter -> {
                    output.writeByte(ParamType.Double.ordinal)
                    output.writeDouble(it.value)
                }
                is ReferenceParameter -> {
                    output.writeByte(ParamType.Ref.ordinal)
                    val value = it.value
                    if (value == null) {
                        writeExternalizableObject(null, null, output)
                    } else {
                        val externalizable = value as? Externalizable ?: error("Parameter is not Externalizable")
                        val kClassMetadata = it.metadata as? KClassMetadata
                                ?: error("Requires KClass metadata for serialization with Externalizable support")
                        val className = requireNotNull(kClassMetadata.kClass.java.name)
                        writeExternalizableObject(externalizable, className, output)
                    }
                }
            }
        }
        output.writeInt(message.trace.size)
        message.trace.forEach {
            output.writeUTF(it)
        }
    }

    enum class ParamType {
        // cannot have more than 8 and order matters
        Ref, Bool, Byte, Short, Int, Long, Float, Double
    }

    private companion object {
        val logger = getLogger<ExternalizableMessage>()
    }
}

private fun writeExternalizableObject(value: Externalizable?, className: String?, output: ObjectOutput) {
    if (value == null) {
        output.writeBoolean(true)
    } else {
        output.writeBoolean(false)
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        output.writeUTF(className) // todo security breach. point to improve in the future
        val externalizable = value as? Externalizable ?: error("Parameter is not Externalizable")
        externalizable.writeExternal(output)
    }
}

private fun readExternalizableObject(input: ObjectInput, logger: Logger): Externalizable? {
    val isNull = input.readBoolean()
    return if (isNull) {
        logger.trace("Null ref param")
        null
    } else {
        val className = input.readUTF()
        logger.trace("Non null ref param with class: {}", className)
        val clazz = Class.forName(className)
        val instance = createAndReadInstance(clazz, input)
        return instance as Externalizable?
    }
}

private fun createAndReadInstance(clazz: Class<out Any>, input: ObjectInput): Any {
    // newInstance is evil, but Externalizable requires a no args constructor
    val instance = clazz.newInstance() as? Externalizable ?: error("Object instance is not externalizable")
    instance.readExternal(input)
    return instance
}

class ExternalizableResponse() : Externalizable {

    lateinit var response: Response

    constructor(msg: Response) : this() {
        response = msg
    }

    constructor(input: ObjectInput) : this() {
        readExternal(input)
    }

    override fun readExternal(input: ObjectInput) {
        logger.debug("Start reading response")
        val type = input.readByte()
        val result: Response = when(Type.values()[type.toInt()]) {
            Type.Unit -> UnitResponse()
            Type.Value -> {
                val externalizable = readExternalizableObject(input, logger)
                ValueResponse(externalizable)
            }
            Type.Exception -> {
                val className = input.readUTF()
                val message: String? = input.readUTF().orNull()
                val stackSize = input.readInt()
                val stackTrace = (0 until stackSize).map {
                    StackElement(
                            input.readUTF(), //className
                            input.readUTF(), //methodName
                            input.readUTF().orNull(), //fileName
                            input.readInt() //lineNumber
                    )
                }
                ExceptionResponse(ExceptionReturn(
                        Class.forName(className).asSubclass(Exception::class.java),
                        message,
                        stackTrace
                ))
            }
        }
        logger.trace("Finished reading response: {}", result)
        response = result
    }

    override fun writeExternal(output: ObjectOutput) {
        when(response.returnType) {
            ReturnType.UNIT -> {
                logger.trace("Writing unit response")
                output.writeByte(Type.Unit.ordinal)
            }
            ReturnType.VALUE -> {
                output.writeByte(Type.Value.ordinal)
                val value = (response.returnValue as ValueReturn).value
                val className = value?.javaClass?.name
                logger.debug("Writing value response: {}", value)
                writeExternalizableObject(value as Externalizable?, className, output)
            }
            ReturnType.EXCEPTION -> {
                output.writeByte(Type.Exception.ordinal)
                val exceptionReturn = response.returnValue as ExceptionReturn
                logger.trace("Writing exception response: {}", exceptionReturn)
                output.writeUTF(exceptionReturn.klass.name)

                // takes some liberties to simplify encoding
                val message = exceptionReturn.message
                output.writeUTF(message ?: NULL_STR)

                val stacktrace = exceptionReturn.stackTrace
                output.writeInt(stacktrace?.size ?: 0)
                stacktrace?.forEach {
                    output.writeUTF(it.className)
                    output.writeUTF(it.methodName)
                    output.writeUTF(it.fileName ?: NULL_STR)
                    output.writeInt(it.lineNumber)
                }
            }
        }
    }

    private enum class Type {
        Unit, Value, Exception
    }

    private fun String.orNull(): String? = if (this == NULL_STR) null else this

    private companion object {
        val logger = getLogger<ExternalizableResponse>()
        const val NULL_STR = "NULL_CONSTANT"
    }
}

