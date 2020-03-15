package pt.pak3nuh.hollywood.processor.generator.context

import com.squareup.kotlinpoet.AnnotationSpec
import pt.pak3nuh.hollywood.processor.Generated
import java.time.Instant

fun GenerationContext.buildGenerationAnnotation(): AnnotationSpec {
    val existing = get(GenerationAnnotation)
    if (existing != null) {
        return existing
    }

    val generatedAnnotation = AnnotationSpec.builder(Generated::class)
            .addMember("%S", Instant.now())
            .build()

    set(GenerationAnnotation, generatedAnnotation)
    return generatedAnnotation
}
