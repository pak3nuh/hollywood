package pt.pak3nuh.hollywood.processor.generator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class KotlinNamedParameterTest {
    @Test
    internal fun `should call with correct parameters`() {
        val greeter: I1 = C1()
        val greet = greeter.greet(s2 = "Maria", s1 = "Hello")
        assertEquals("Hello Maria", greet)
    }
}

private interface I1 {
    fun greet(s1: String, s2: String): String
}

private class C1: I1 {
    override fun greet(prefix: String, greeterName: String): String {
        return "$prefix $greeterName"
    }
}
