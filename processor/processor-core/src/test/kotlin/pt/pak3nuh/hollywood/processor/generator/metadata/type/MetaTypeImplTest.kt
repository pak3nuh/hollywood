package pt.pak3nuh.hollywood.processor.generator.metadata.type

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.KmType
import org.junit.jupiter.api.Test

internal class MetaTypeImplTest{
    @Test
    internal fun `should array`() {
        assertType("kotlin/Array", true)
        assertType("kotlin/IntArray", true)
        assertType("kotlin/String", false)
    }

    private fun assertType(name: String, isArray: Boolean) {
        val kmType = KmType(0)
        kmType.classifier = KmClassifier.Class(name)
        val typeImpl = MetaTypeImpl(kmType, emptyList())
        assertThat(typeImpl.isArray).isEqualTo(isArray)
    }
}