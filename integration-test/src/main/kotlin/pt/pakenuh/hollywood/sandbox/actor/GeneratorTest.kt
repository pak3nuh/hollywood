package pt.pakenuh.hollywood.sandbox.actor

import pt.pak3nuh.hollywood.processor.Actor
// todo make 2 test suites, one for metadata enabled and another for disabled. check detekt for gradle runs on junit
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
            byteArray: ByteArray?, booleanArray: BooleanArray, shortArray: ShortArray, intArray: IntArray,
            longArray: LongArray, floatArray: FloatArray?, doubleArray: DoubleArray, referenceArray: Array<String>?,
            wrappedBooleanArray: Array<Boolean?>
    )

    suspend fun multiDimensionalArrays(string: Array<Array<String>>, int: Array<IntArray>, long: Array<LongArray>)

    suspend fun usualCollectionTypes(
            list: List<String>?, set: Set<String>, map: Map<String, String>
    )

    suspend fun wildcardGeneric(generic: Generic<*>?)

    suspend fun supperBound(generic: Generic<in String?>)

    suspend fun extendsBound(generic: Generic<out String>)

//    fun nonSuspendingWillBreak()

    suspend fun withDefaultBehaviour(string: String): CharArray {
        return string.toCharArray()
    }

    suspend fun nullableType(p1: Int?): List<String>?

    suspend fun nullableGeneric(p1: Generic<String?>): List<String?>

    // not supported by design
    //suspend fun <T> typeParameter(p1: T)

    // different signature between nullable array and primitive array
    suspend fun arrayNullableOverload(primitiveArray: Array<Int?>)
    suspend fun arrayNullableOverload(primitiveArray: IntArray)

    suspend fun typeAliasWithGeneric(p1: Gen<String?>)

    suspend fun nothingFunction(): Nothing

//    suspend fun actorParametersNotAllowed(p1: TypeMappingActor)
//    suspend fun actorParametersNotAllowed(): TypeMappingActor
}

typealias Gen<T> = Generic<T>

class Generic<T>
