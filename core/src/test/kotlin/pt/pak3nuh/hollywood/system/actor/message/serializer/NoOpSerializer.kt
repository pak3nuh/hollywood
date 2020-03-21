package pt.pak3nuh.hollywood.system.actor.message.serializer

import pt.pak3nuh.hollywood.actor.message.Message
import pt.pak3nuh.hollywood.actor.message.serializer.Serializer

class NoOpSerializer: Serializer {
    override fun serialize(message: Message): ByteArray {
        throw UnsupportedOperationException()
    }
}
