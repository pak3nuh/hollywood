package pt.pak3nuh.hollywood.processor.generator.metadata.type

import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.UNIT
import kotlin.reflect.KClass

object WellKnownTypes {
    val unitType: MetaType = WKType(Unit::class, UNIT)
}

private class WKType(kClass: KClass<*>, private val typeName: TypeName) : MetaType {

    override val name: String = kClass.qualifiedName!!

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
