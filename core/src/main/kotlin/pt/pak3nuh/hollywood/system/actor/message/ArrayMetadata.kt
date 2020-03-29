package pt.pak3nuh.hollywood.system.actor.message

import pt.pak3nuh.hollywood.actor.message.KClassMetadata
import kotlin.reflect.KClass

class ArrayMetadata(private val component: KClass<*>) : KClassMetadata {

    override val kClass: KClass<*>
        get() = component
}
