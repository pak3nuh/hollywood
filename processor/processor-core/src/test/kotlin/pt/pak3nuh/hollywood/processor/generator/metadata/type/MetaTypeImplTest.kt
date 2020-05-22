package pt.pak3nuh.hollywood.processor.generator.metadata.type

import assertk.assertThat
import assertk.assertions.isTrue
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.KmType
import org.junit.jupiter.api.Test

internal class MetaTypeImplTest{
    @Test
    internal fun `should array`() {
        val kmType = KmType(0)
        kmType.classifier = KmClassifier.Class("kotlin.Array")
        val typeImpl = MetaTypeImpl(kmType, emptyList())
        assertThat(typeImpl.isArray).isTrue()
    }
}