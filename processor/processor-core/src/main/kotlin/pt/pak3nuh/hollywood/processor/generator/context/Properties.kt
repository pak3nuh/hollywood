package pt.pak3nuh.hollywood.processor.generator.context

import com.squareup.kotlinpoet.AnnotationSpec
import pt.pak3nuh.hollywood.processor.Generated
import java.time.Instant

fun GenerationContext.generationAnnotation(): AnnotationSpec {
    val existing = get(GenerationAnnotation)
    if (existing != null) {
        return existing.annotationSpec
    }

    val generatedAnnotation = AnnotationSpec.builder(Generated::class)
            .addMember("%S", Instant.now())
            .build()

    set(GenerationAnnotation, GenerationAnnotation(generatedAnnotation))
    return generatedAnnotation
}
