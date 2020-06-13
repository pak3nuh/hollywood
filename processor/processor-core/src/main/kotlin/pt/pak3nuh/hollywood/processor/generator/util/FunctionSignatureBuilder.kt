package pt.pak3nuh.hollywood.processor.generator.util

import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.TypeName
import pt.pak3nuh.hollywood.processor.generator.MethodResult.FunSignature

internal class FunctionSignatureBuilder {

    private val parcels = mutableListOf<Parcel>()

    fun build(functionName: String): FunSignature {
        require(functionName.isNotBlank()) { "Function name must be valid" }
        val id = "$functionName?${parcels.map(Parcel::id).joinToString("&")}"
        return FunSignature(id, id)
    }

    fun addReference(typeName: TypeName): FunctionSignatureBuilder {
        // we didn't need to include parameterized types here, but arrays are parameterized in kotlin
        // and reified in the JVM
        // todo optimize
        val typeNameAsString = typeName.toString()
        val typeId = typeNameAsString
                .replace('.', '_')
                .replace('<', '(')
                .replace('>', ')')
                // todo kotlinpoet has a bug that breaks lines on signatures if they are large and contain spaces
                .replace(" ", "")
        parcels.add(Parcel(typeId, typeName))
        return this
    }

    fun addByte(): FunctionSignatureBuilder {
        parcels.add(Parcel("BYTE", BYTE))
        return this
    }

    fun addBoolean(): FunctionSignatureBuilder {
        parcels.add(Parcel("BOOL", BOOLEAN))
        return this
    }

    fun addShort(): FunctionSignatureBuilder {
        parcels.add(Parcel("SRT", SHORT))
        return this
    }

    fun addInt(): FunctionSignatureBuilder {
        parcels.add(Parcel("INT", INT))
        return this
    }

    fun addLong(): FunctionSignatureBuilder {
        parcels.add(Parcel("LNG", LONG))
        return this
    }

    fun addFloat(): FunctionSignatureBuilder {
        parcels.add(Parcel("FLT", FLOAT))
        return this
    }

    fun addDouble(): FunctionSignatureBuilder {
        parcels.add(Parcel("DBL", DOUBLE))
        return this
    }

    private data class Parcel(val id: String, val typeName: TypeName)

}
