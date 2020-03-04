package pt.pak3nuh.hollywood.processor.generator

import com.squareup.kotlinpoet.FileSpec
import java.nio.file.Files
import java.nio.file.Path

interface Generator {
    fun generate(): SourceFile
}

class SourceFile(private val fileSpec: FileSpec) {
    fun writeTo(folder: Path) {
        val lastPackage = fileSpec.packageName
                .splitToSequence('.')
                .fold(folder) { actual: Path, subPkg: String ->
                    val newPath = actual.resolve(subPkg)
                    if (!Files.exists(newPath)) {
                        Files.createDirectory(newPath)
                    }
                    newPath
                }

        val filePath = lastPackage.resolve("${fileSpec.name}.kt")
        Files.deleteIfExists(filePath)
        Files.createFile(filePath)
    }
}