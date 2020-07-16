package pt.pak3nuh.hollywood.system.actor.message.serializer

import pt.pak3nuh.hollywood.actor.message.Message
import pt.pak3nuh.hollywood.actor.message.Response
import java.io.InputStream
import java.io.OutputStream

internal interface InternalSerDes {
    fun serialize(message: Message, stream: OutputStream)
    fun serialize(response: Response, stream: OutputStream)
    fun supports(message: Message): Boolean
    fun supports(response: Response): Boolean
    fun deserializeMessage(stream: InputStream): Message
    fun deserializeResponse(stream: InputStream): Response
}