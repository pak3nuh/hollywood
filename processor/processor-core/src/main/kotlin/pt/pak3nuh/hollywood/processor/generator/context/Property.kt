package pt.pak3nuh.hollywood.processor.generator.context

import com.squareup.kotlinpoet.AnnotationSpec

sealed class Property<T>

object GenerationAnnotation: Property<AnnotationSpec>()