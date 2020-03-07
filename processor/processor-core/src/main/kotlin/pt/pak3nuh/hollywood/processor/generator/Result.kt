package pt.pak3nuh.hollywood.processor.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec

sealed class Result

class TypeResult(private val className: ClassName, private val typeSpec: TypeSpec) : Result() {
    fun toSourceFile(): SourceFile {
        val fileSpec = FileSpec.get(className.packageName, typeSpec)
        return SourceFile(fileSpec)
    }
}

class MethodResult(val funSpec: FunSpec) : Result()

object NoOpResult : Result()
