package pt.pak3nuh.hollywood.processor.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec

sealed class Result

sealed class Writer: Result() {
    abstract fun toWriter(): FileWriter
}

class Bundle(private val types: List<TypeResult>): Writer() {
    override fun toWriter(): FileWriter {
        return SourceBundle(types.map(TypeResult::toWriter))
    }
}

class TypeResult(private val className: ClassName, private val typeSpec: TypeSpec) : Writer() {
    override fun toWriter(): FileWriter {
        val fileSpec = FileSpec.get(className.packageName, typeSpec)
        return SourceFile(fileSpec)
    }
}

class MethodResult(val funSpec: FunSpec, val funSignature: FunSignature) : Result() {

    /**
     * @param symbolName A valid kotlin symbol for a constant
     * @param value The symbol value
     */
    data class FunSignature(val symbolName: String, val value: String)

}

object NoOpResult : Result()
