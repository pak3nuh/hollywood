package pt.pak3nuh.hollywood.processor.generator

import com.squareup.kotlinpoet.FileSpec
import pt.pak3nuh.hollywood.processor.generator.context.GenerationContext
import java.nio.file.Path
import javax.lang.model.element.TypeElement

interface FileGenerator {
    fun generate(element: TypeElement, context: GenerationContext): FileWriter
}

interface FileWriter {
    fun writeTo(folder: Path)
}

class SourceFile(private val fileSpec: FileSpec): FileWriter {
    override fun writeTo(folder: Path) {
        fileSpec.writeTo(folder)
    }

    override fun toString(): String {
        return "SourceFile(path=${fileSpec.packageName}.${fileSpec.name})"
    }
}

class SourceBundle(private val files: List<FileWriter>): FileWriter {
    override fun writeTo(folder: Path) {
        files.forEach { it.writeTo(folder) }
    }
}
