package pt.pak3nuh.hollywood.system

import pt.pak3nuh.hollywood.actor.ActorFactory
import pt.pak3nuh.hollywood.actor.proxy.ActorProxy
import kotlin.reflect.KClass
import kotlin.reflect.full.cast

fun simpleRepository(vararg factories: ActorFactory<*, *>): FactoryRepository {
    return object : FactoryRepository {
        override fun <T : Any, P : ActorProxy<T>, F : ActorFactory<T, P>> get(factoryClass: KClass<out F>): F {
            val item = factories.firstOrNull { it::class == factoryClass } ?: throw NoSuchElementException()
            return factoryClass.cast(item)
        }

    }
}
