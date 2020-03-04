package pt.pak3nuh.hollywood.processor.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec

sealed class Result

class TypeResult(private val typeSpec: TypeSpec) : Result() {
    fun toSourceFile(): SourceFile {
        val typeName = requireNotNull(typeSpec.name) { "Type name is mandatory" }
        val asClassName = ClassName.bestGuess(typeName)
        val fileSpec = FileSpec.get(asClassName.packageName, typeSpec)
        return SourceFile(fileSpec)
    }
}

object NoOpResult: Result()