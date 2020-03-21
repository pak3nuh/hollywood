package pt.pak3nuh.hollywood.system.actor.message.serializer

import pt.pak3nuh.hollywood.actor.message.Message
import pt.pak3nuh.hollywood.actor.message.Parameter

data class TestMessage(
        override val functionId: String,
        override val parameters: List<Parameter>
) : Message {
    override val returnValue
        get() = error("Property not available")
}
