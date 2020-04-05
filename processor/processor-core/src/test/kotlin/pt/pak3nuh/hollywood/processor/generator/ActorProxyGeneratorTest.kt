package pt.pak3nuh.hollywood.processor.generator

import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import javax.lang.model.element.ElementKind

internal class ActorProxyGeneratorTest {
    @Test
    internal fun `should only accept interfaces`() {
        val generator = ActorProxyGenerator(mockk(), mockk())
        assertThrows<IllegalArgumentException> {
            generator.generate(TypeElementStub(ElementKind.ANNOTATION_TYPE), mockk())
        }
    }
}
