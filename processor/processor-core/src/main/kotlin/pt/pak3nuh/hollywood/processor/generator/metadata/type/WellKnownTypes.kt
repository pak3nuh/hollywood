package pt.pak3nuh.hollywood.processor.generator.metadata.type

import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.UNIT
import kotlin.reflect.KClass

object WellKnownTypes {
    val unitType: MetaType = WKType(Unit::class, UNIT)

    val boolean: MetaType = WKType(Boolean::class, BOOLEAN)
    val byte: MetaType = WKType(Byte::class, BYTE)
    val short: MetaType = WKType(Short::class, SHORT)
    val int: MetaType = WKType(Int::class, INT)
    val float: MetaType = WKType(Float::class, FLOAT)
    val long: MetaType = WKType(Long::class, LONG)
    val double: MetaType = WKType(Double::class, DOUBLE)
}

private class WKType(kClass: KClass<*>, private val typeName: TypeName) : MetaType {

    override val name: String = kClass.qualifiedName!!
    override val isArray: Boolean = false

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is MetaType) {
            return false
        }
        // ignore nullability for these types
        return other.name == name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun asTypeName(): TypeName = typeName

}
