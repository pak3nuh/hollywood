package pt.pak3nuh.hollywood.system.actor.message.serializer.kotlin

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test
import pt.pak3nuh.hollywood.actor.message.ValueResponse
import java.math.BigDecimal

internal class KotlinSerDesDiscoveryTest {
    @Test
    internal fun `should discover built in types`() {
        val serDesDiscovery = KotlinSerDesDiscovery()
        assertSupports(null, serDesDiscovery)
        assertSupports(1.toByte(), serDesDiscovery)
        assertSupports(1.toShort(), serDesDiscovery)
        assertSupports(1, serDesDiscovery)
        assertSupports(1L, serDesDiscovery)
        assertSupports(1f, serDesDiscovery)
        assertSupports(1.0, serDesDiscovery)
        assertSupports(true, serDesDiscovery)
        assertSupports("", serDesDiscovery)
    }

    @Test
    internal fun `should not support builtin`() {
        val serDesDiscovery = KotlinSerDesDiscovery()
        assertThat(serDesDiscovery.supports(ValueResponse(BigDecimal.ONE))).isFalse()
    }

    private fun assertSupports(value: Any?, serDesDiscovery: KotlinSerDesDiscovery) {
        assertThat(serDesDiscovery.supports(ValueResponse(value))).isTrue()
    }
}