package pt.pak3nuh.hollywood.system.actor.message

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class FunctionSignatureBuilderTest {

    private val builder = FunctionSignatureBuilder()

    @Test
    internal fun `should build no parameter signatures`() {
        val name = builder.build("function")
        assertThat(name).isEqualTo("function:")
    }

    @Test
    internal fun `should build primitive parameter signatures`() {
        val name = builder.addBoolean().addByte().addDouble().addFloat().addInt().addLong().build("a")
        assertThat(name).isEqualTo("a:Z;B;D;F;I;J")
    }

    @Test
    internal fun `should build reference signatures`() {
        val name = builder.addReference(String::class, false)
                .addReference(Int::class, true)
                .build("b")
        assertThat(name).isEqualTo("b:Lkotlin.String;Lkotlin.Int?")
    }

    @Test
    internal fun `should build multi dimensional array signatures`() {
        val name = builder.addArray {
            nest(false) {
                nest(true) {
                    nest(false) {
                        component(IntArray::class, false)
                    }
                }
            }
        }.build("d")
        assertThat(name).isEqualTo("d:[[[Lkotlin.IntArray]]?]")
    }

    @Test
    internal fun `shouldn't allow multiple nest calls on the same scope`() {
        assertThrows<IllegalStateException> {
            builder.addArray {
                nest(true) {}
                nest(false) {}
            }
        }
    }

    @Test
    internal fun `should assume Any if no component is specified`() {
        val name = builder.addArray { }.build("e")
        assertThat(name).isEqualTo("e:[Lkotlin.Any]")

        val another = FunctionSignatureBuilder().addArray { nest(false) {} }.build("e")
        assertThat(another).isEqualTo("e:[[Lkotlin.Any]]")
    }

    @Test
    internal fun `should require function name`() {
        assertThrows<IllegalArgumentException> {
            builder.build("")
        }
    }
}
