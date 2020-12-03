package pt.pak3nuh.hollywood.system.actor.message

import pt.pak3nuh.hollywood.actor.message.Message
import pt.pak3nuh.hollywood.actor.message.Parameter

internal class MessageImpl(
        override val functionId: String,
        override val parameters: List<Parameter>,
        override val trace: Set<String>
) : Message
