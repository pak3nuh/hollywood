package pt.pak3nuh.hollywood.actor.message

sealed class ReturnValue

/**
 * A special response of [Unit].
 */
@Suppress("CanSealedSubClassBeObject") // serializers don't like object definitions
class UnitReturn: ReturnValue()

/**
 * Successful response with a value.
 */
// specialize primitives for optimization if needed
data class ValueReturn(val value: Any?): ReturnValue()

/**
 * Failed function execution that ended with an exception being thrown.
 */
// exceptions can lead to serialization overflow
data class ExceptionReturn(
        val klass: Class<out Exception>,
        val message: String?,
        val stackTrace: List<StackElement>?
): ReturnValue() {
    constructor(exception: Exception):
            this(exception::class.java, exception.message, exception.stackTrace?.map(::mapStackElement))
}

/**
 * Class to model stacktrace lines. I don't use [StackTraceElement] because it differs starting on JDK9+.
 *
 * Multi-release jar was considered, but adds accidental complexity on the build process.
 */
data class StackElement(val className: String, val methodName: String, val fileName: String?, val lineNumber: Int) {
    fun toJdkStackElement() = StackTraceElement(className, methodName, fileName, lineNumber)
}

fun mapStackElement(jdkElement: StackTraceElement): StackElement = StackElement(
        jdkElement.className,
        jdkElement.methodName,
        jdkElement.fileName,
        jdkElement.lineNumber
)
