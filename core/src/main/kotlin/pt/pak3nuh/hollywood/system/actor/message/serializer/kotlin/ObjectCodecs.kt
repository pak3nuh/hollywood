package pt.pak3nuh.hollywood.system.actor.message.serializer.kotlin

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.CompositeEncoder
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.UpdateMode
import kotlinx.serialization.modules.SerialModule
import java.io.ObjectInput
import java.io.ObjectOutput

// todo
class OutputStreamEncoder(override val context: SerialModule, private val stream: ObjectOutput) : Encoder {
    override fun beginStructure(descriptor: SerialDescriptor, vararg typeSerializers: KSerializer<*>): CompositeEncoder {
        TODO("Not yet implemented")
    }

    override fun encodeBoolean(value: Boolean) {
        stream.writeBoolean(value)
    }

    override fun encodeByte(value: Byte) {
        stream.writeByte(value.toInt())
    }

    override fun encodeChar(value: Char) {
        stream.writeChar(value.toInt())
    }

    override fun encodeDouble(value: Double) {
        stream.writeDouble(value)
    }

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        TODO("Not yet implemented")
    }

    override fun encodeFloat(value: Float) {
        stream.writeFloat(value)
    }

    override fun encodeInt(value: Int) {
        stream.writeInt(value)
    }

    override fun encodeLong(value: Long) {
        stream.writeLong(value)
    }

    override fun encodeNull() {
        TODO("Not yet implemented")
    }

    override fun encodeShort(value: Short) {
        stream.writeShort(value.toInt())
    }

    override fun encodeString(value: String) {
        stream.writeUTF(value)
    }

    override fun encodeUnit() {
        TODO("Not yet implemented")
    }

}

class InputStreamDecoder(override val context: SerialModule, override val updateMode: UpdateMode, val objectInput: ObjectInput) : Decoder {
    override fun beginStructure(descriptor: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeDecoder {
        TODO("Not yet implemented")
    }

    override fun decodeBoolean(): Boolean {
        TODO("Not yet implemented")
    }

    override fun decodeByte(): Byte {
        TODO("Not yet implemented")
    }

    override fun decodeChar(): Char {
        TODO("Not yet implemented")
    }

    override fun decodeDouble(): Double {
        TODO("Not yet implemented")
    }

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
        TODO("Not yet implemented")
    }

    override fun decodeFloat(): Float {
        TODO("Not yet implemented")
    }

    override fun decodeInt(): Int {
        TODO("Not yet implemented")
    }

    override fun decodeLong(): Long {
        TODO("Not yet implemented")
    }

    override fun decodeNotNullMark(): Boolean {
        TODO("Not yet implemented")
    }

    override fun decodeNull(): Nothing? {
        TODO("Not yet implemented")
    }

    override fun decodeShort(): Short {
        TODO("Not yet implemented")
    }

    override fun decodeString(): String {
        TODO("Not yet implemented")
    }

    override fun decodeUnit() {
        TODO("Not yet implemented")
    }
}