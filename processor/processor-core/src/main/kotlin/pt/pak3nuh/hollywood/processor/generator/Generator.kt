package pt.pak3nuh.hollywood.processor.generator

import com.squareup.kotlinpoet.FileSpec
import java.nio.file.Path

interface Generator {
    fun generate(): SourceFile
}

class SourceFile(private val fileSpec: FileSpec) {
    fun writeTo(folder: Path) {
        fileSpec.writeTo(folder)
    }

}