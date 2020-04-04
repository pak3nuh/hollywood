package pt.pak3nuh.hollywood.system.actor.message

import kotlinx.coroutines.CompletableDeferred
import pt.pak3nuh.hollywood.actor.message.Message
import pt.pak3nuh.hollywood.actor.message.Parameter
import pt.pak3nuh.hollywood.actor.message.ReturnValue

internal class MessageImpl(
        override val functionId: String,
        override val parameters: List<Parameter>
) : Message {

    override val returnValue: CompletableDeferred<ReturnValue> = CompletableDeferred()

}
