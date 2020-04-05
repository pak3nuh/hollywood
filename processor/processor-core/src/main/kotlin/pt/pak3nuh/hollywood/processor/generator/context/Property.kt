package pt.pak3nuh.hollywood.processor.generator.context

import com.squareup.kotlinpoet.AnnotationSpec

interface Property<T>

class GenerationAnnotation(val annotationSpec: AnnotationSpec) {
    companion object Key: Property<GenerationAnnotation>
}
