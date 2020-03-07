package pt.pak3nuh.hollywood.processor.generator

import com.squareup.kotlinpoet.FileSpec
import pt.pak3nuh.hollywood.processor.generator.context.GenerationContext
import java.nio.file.Path
import javax.lang.model.element.TypeElement

interface FileGenerator {
    fun generate(element: TypeElement, context: GenerationContext): SourceFile
}

class SourceFile(private val fileSpec: FileSpec) {
    fun writeTo(folder: Path) {
        fileSpec.writeTo(folder)
    }

    override fun toString(): String {
        return "SourceFile(path=${fileSpec.packageName}.${fileSpec.name})"
    }
}
