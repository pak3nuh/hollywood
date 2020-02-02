package pt.pak3nuh.hollywood.system.builder

import assertk.assertThat
import assertk.assertions.hasClass
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class SystemBuilderTest {
    @Test
    internal fun `should build system and create actor`() {
        val actorSystem = SystemBuilder()
                .registerFactory(GreeterFactory())
                .build()

        val actor = actorSystem.actorManager.createActor(GreeterFactory::class) {
            it.createActor()
        }

        assertThat(actor).hasClass(GreeterProxy::class)
        val hello = actor.sayHello()
        assertThat(hello).isEqualTo("Hello World!")
    }

    @Test
    internal fun `should not accept proxy that doesn't implement the actor type`() {
        assertThrows<IllegalArgumentException> {
            SystemBuilder().registerFactory(ClockFactory())
        }
    }

    @Test
    internal fun `should not accept self referenced proxy`() {
        assertThrows<IllegalArgumentException> {
            SystemBuilder().registerFactory(PersonFactory())
        }
    }
}
