package pt.pak3nuh.hollywood.actor.proxy

import pt.pak3nuh.hollywood.processor.Actor

/**
 * Base interface that must be present in all the proxies.
 */
interface ActorProxy<T> {
    /**
     * The actual user defined actor.
     *
     * Because an [ActorProxy] of [T] is also a [T], it is possible to just return **this**, although highly discouraged,
     * because it goes against the expected structure.
     * @see Actor
     */
    val delegate: T

    /**
     * A unique ID for each proxy.
     */
    val proxyId: String

    /**
     * Closes all associated proxy resources.
     */
    fun dispose()
}

/**
 * Contains additional data for proxy behaviour.
 */
interface ProxyConfiguration {
    val proxyId: String
}
