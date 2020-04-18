package pt.pak3nuh.hollywood.system.actor.message.serializer

import pt.pak3nuh.hollywood.actor.message.Message
import pt.pak3nuh.hollywood.actor.message.Response
import pt.pak3nuh.hollywood.actor.message.serializer.Deserializer
import pt.pak3nuh.hollywood.actor.message.serializer.Serializer

class NoOpSerde: Serializer, Deserializer {
    override fun serialize(message: Message): ByteArray {
        throw UnsupportedOperationException()
    }

    override fun serialize(response: Response): ByteArray {
        throw UnsupportedOperationException()
    }

    override fun asMessage(byteArray: ByteArray): Message {
        throw UnsupportedOperationException()
    }

    override fun asResponse(byteArray: ByteArray): Response {
        throw UnsupportedOperationException()
    }
}
