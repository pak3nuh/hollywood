package pt.pakenuh.hollywood.sandbox.actor

import pt.pak3nuh.hollywood.processor.Actor

/**
 * Generated actor must map java types to kotlin types in both parameters and return types
 * If the generated code compiles, the test passes
 */
@Actor
interface TypeMappingActor {
    suspend fun returnWrappedBooleanList(): List<Boolean>
    suspend fun everyPrimitiveType(
            byte: Byte, boolean: Boolean, short: Short, int: Int, long: Long, float: Float, double: Double
    )
    suspend fun everyArrayType(
            byteArray: ByteArray, booleanArray: BooleanArray, shortArray: ShortArray, intArray: IntArray,
            longArray: LongArray, floatArray: FloatArray, doubleArray: DoubleArray, referenceArray: Array<String>
    )
    suspend fun usualCollectionTypes(
            list: List<String>, set: Set<String>, map: Map<String, String>
    )
    suspend fun wildcardGeneric(generic: Generic<*>)
    suspend fun supperBound(generic: Generic<in String>)
    suspend fun extendsBound(generic: Generic<out String>)
}

class Generic<T>
