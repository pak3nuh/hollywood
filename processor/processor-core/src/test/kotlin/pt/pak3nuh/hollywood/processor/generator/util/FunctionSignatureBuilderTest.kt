package pt.pak3nuh.hollywood.processor.generator.util

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import org.junit.jupiter.api.Test

internal class FunctionSignatureBuilderTest{
    private val builder = FunctionSignatureBuilder()

    @Test
    internal fun `should build equal symbols and values`() {
        // not a hard requirement, but we can piggyback on the uniqueness of symbols to avoid
        // bugs downstream
        val signature = builder.addBoolean().addByte().addDouble().addFloat().addInt().addLong()
                .addReference(String::class.asTypeName())
                .build("equal symbols and values")
        assertThat(signature.symbolName).isEqualTo(signature.value)
    }

    @Test
    internal fun `should build no parameter signatures`() {
        val signature = builder.build("function")
        assertThat(signature.symbolName).isEqualTo("function?")
    }

    @Test
    internal fun `should build primitive parameter signatures`() {
        val signature = builder.addBoolean().addByte().addDouble().addFloat().addInt().addLong().build("a")
        assertThat(signature.symbolName).isEqualTo("a?BOOL&BYTE&DBL&FLT&INT&LNG")
    }

    @Test
    internal fun `should build reference signatures`() {
        val strClass = ClassName.bestGuess(String::class.qualifiedName!!)
        val intClass = ClassName.bestGuess(Int::class.qualifiedName!!).copy(nullable = true)
        val signature = builder.addReference(strClass)
                .addReference(intClass)
                .build("b")
        assertThat(signature.symbolName).isEqualTo("b?kotlin_String&kotlin_Int?")
    }

    @Test
    internal fun `should build multi dimentional arrays`() {
        val parameterizedBy: TypeName = Array<String>::class.parameterizedBy(String::class).copy(nullable = true)
        val typeName = Array<Any?>::class.asClassName().parameterizedBy(parameterizedBy)
        val signature = builder.addReference(typeName)
                .build("multi dimentional arrays")

        assertThat(signature.symbolName).isEqualTo("multi dimentional arrays?kotlin_Array(kotlin_Array(kotlin_String)?)")
    }

    @Test
    internal fun `should build primitive arrays`() {
        val signature = builder.addReference(IntArray::class.asTypeName())
                .build("primitive array")

        assertThat(signature.symbolName).isEqualTo("primitive array?kotlin_IntArray")
    }

    @Test
    internal fun `should build parameterized types`() {
        // we cannot strip out the parameterized part because arrays are not subject to erasure
        val typeName = Box::class.parameterizedBy(String::class)
        val signature = builder.addReference(typeName)
                .build("parameterized")
        assertThat(signature.symbolName).isEqualTo("parameterized?pt_pak3nuh_hollywood_processor_generator_util_Box(kotlin_String)")
    }
}

private class Box<T>

private class Overloads {
    fun arrayOverload(array: Array<Array<String>>) {}
    fun arrayOverload(array: Array<Array<Int>>) {}
}

private class Identifiers {
    val `allowed charaters !"#$%&()=?-_~^*+` = ""
}
