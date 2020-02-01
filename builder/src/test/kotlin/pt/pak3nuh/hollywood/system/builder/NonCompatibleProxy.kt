package pt.pak3nuh.hollywood.system.builder

import pt.pak3nuh.hollywood.actor.ActorFactory
import pt.pak3nuh.hollywood.actor.proxy.ProxyConfiguration
import java.time.Instant
import kotlin.reflect.KClass

interface Clock {
    fun currentTime(): Instant
}

class ClockImpl : Clock {
    override fun currentTime(): Instant = Instant.now()
}

class ProxyClock(override val delegate: Clock) : BaseProxy<Clock>()

class ClockFactory : ActorFactory<Clock, ProxyClock> {
    override fun createProxy(delegate: Clock, config: ProxyConfiguration): ProxyClock = ProxyClock(delegate)
    override val actorKClass: KClass<Clock> = Clock::class
    override val proxyKClass: KClass<ProxyClock> = ProxyClock::class
}
