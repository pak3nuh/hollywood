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
                .registerFactory(GreeterFactory::class) { _, _ ->
                    GreeterFactory()
                }
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
        assertThrows<IllegalStateException> {
            SystemBuilder().registerFactory(ClockFactory::class) { _, _ ->
                ClockFactory()
            }.build()
        }
    }

    @Test
    internal fun `should not accept self referenced proxy`() {
        assertThrows<IllegalStateException> {
            SystemBuilder().registerFactory(PersonFactory::class) { _, _ ->
                PersonFactory()
            }.build()
        }
    }

    private object MyProp : SystemBuilder.Property<String>() {}

    @Test
    internal fun `should provide properties to factories`() {
        val system = SystemBuilder().withProperty(MyProp) {
            "Custom greet"
        }.registerFactory(GreeterFactory::class) { _, props ->
            GreeterFactory(props[MyProp])
        }.build()

        val greet = system.actorManager
                .createActor(GreeterFactory::class, GreeterFactory::createActor)
                .sayHello()

        assertThat(greet).isEqualTo("Custom greet")
    }

    @Test
    internal fun `should throw if properties are accessed after build`() {
        assertThrows<IllegalStateException> {
            var p: SystemBuilder.PropertyGetter? = null
            SystemBuilder().withProperty(MyProp) { "value" }
                    .registerFactory(GreeterFactory::class) { _, props ->
                        p = props
                        GreeterFactory()
                    }.build()
            p!![MyProp]
        }

    }
}
