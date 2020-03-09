package pt.pak3nuh.hollywood.processor.visitor

import com.squareup.kotlinpoet.ARRAY
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.BOOLEAN_ARRAY
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.BYTE_ARRAY
import com.squareup.kotlinpoet.CHAR
import com.squareup.kotlinpoet.CHAR_ARRAY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.DOUBLE_ARRAY
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.FLOAT_ARRAY
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.INT_ARRAY
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.LONG_ARRAY
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SET
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.SHORT_ARRAY
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.asWildcardTypeName
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.ArrayType
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ErrorType
import javax.lang.model.type.ExecutableType
import javax.lang.model.type.IntersectionType
import javax.lang.model.type.NoType
import javax.lang.model.type.NullType
import javax.lang.model.type.PrimitiveType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.lang.model.type.TypeVariable
import javax.lang.model.type.UnionType
import javax.lang.model.type.WildcardType
import javax.lang.model.util.AbstractTypeVisitor8

class TypeConverter {

    // todo document available mappings
    private val kotlinConversionMap = mapOf<String, ClassName>(
            // immutable because the contents shouldn't change between calls
            "java.util.List" to LIST,
            "java.util.Set" to SET,
            "java.util.Map" to MAP,
            "java.lang.Boolean" to BOOLEAN,
            "java.lang.Byte" to BYTE,
            "java.lang.Short" to SHORT,
            "java.lang.Integer" to INT,
            "java.lang.Long" to LONG,
            "java.lang.Float" to FLOAT,
            "java.lang.Double" to DOUBLE,
            "java.lang.String" to STRING
    )

    fun convert(typeMirror: TypeMirror): TypeName {
        val visitor = Visitor()
        typeMirror.accept(visitor, null)
        return visitor.typeName
    }

    fun convert(typeElement: Element): TypeName {
        // converts to mirrors because of better generic support
        return convert(typeElement.asType())
    }

    private inner class Visitor : AbstractTypeVisitor8<Unit, Unit?>() {

        lateinit var typeName: TypeName

        private fun getRawType(type: DeclaredType): TypeName {
            val asElement = type.asElement() as TypeElement
            val elementName = asElement.qualifiedName.toString()
            return kotlinConversionMap[elementName] ?: asElement.asClassName()
        }
        
        override fun visitPrimitive(type: PrimitiveType, ctx: Unit?) {
            typeName = when (type.kind) {
                TypeKind.BOOLEAN -> BOOLEAN
                TypeKind.BYTE -> BYTE
                TypeKind.SHORT -> SHORT
                TypeKind.INT -> INT
                TypeKind.LONG -> LONG
                TypeKind.CHAR -> CHAR
                TypeKind.FLOAT -> FLOAT
                TypeKind.DOUBLE -> DOUBLE
                else -> error("Primitive type $type is not supported")
            }
        }

        override fun visitDeclared(type: DeclaredType, ctx: Unit?) {
            val raw = getRawType(type)
            typeName = type.typeArguments
                    .map { convert(it) }
                    .fold(raw) { actual: TypeName, typeParameter: TypeName ->
                        addParameter(actual, typeParameter)
                    }
        }

        private fun addParameter(baseType: TypeName, parameterType: TypeName): ParameterizedTypeName {
            return when (baseType) {
                is ClassName -> baseType.parameterizedBy(parameterType)
                is ParameterizedTypeName -> baseType.plusParameter(parameterType)
                else -> error("Unexpected type $baseType")
            }
        }

        override fun visitWildcard(type: WildcardType, ctx: Unit?) {
            val superBound: TypeMirror? = type.superBound
            val extendsBound: TypeMirror? = type.extendsBound
            typeName = when {
                superBound != null -> {
                    val convert = convert(superBound)
                    WildcardTypeName.consumerOf(convert)
                }
                extendsBound != null -> {
                    val convert = convert(extendsBound)
                    WildcardTypeName.producerOf(convert)
                }
                else -> type.asWildcardTypeName()
            }
        }

        override fun visitTypeVariable(type: TypeVariable, ctx: Unit?) {
            typeName = type.asTypeName()
        }

        override fun visitArray(type: ArrayType, ctx: Unit?) {
            val componentType: TypeMirror = type.componentType
            val primitiveArray = when (componentType.kind) {
                TypeKind.BOOLEAN -> BOOLEAN_ARRAY
                TypeKind.BYTE -> BYTE_ARRAY
                TypeKind.SHORT -> SHORT_ARRAY
                TypeKind.INT -> INT_ARRAY
                TypeKind.LONG -> LONG_ARRAY
                TypeKind.CHAR -> CHAR_ARRAY
                TypeKind.FLOAT -> FLOAT_ARRAY
                TypeKind.DOUBLE -> DOUBLE_ARRAY
                else -> null
            }
            typeName = primitiveArray ?: ARRAY.parameterizedBy(convert(componentType))
        }

        override fun visitExecutable(type: ExecutableType, ctx: Unit?) = error("Unsupported")

        override fun visitUnknown(type: TypeMirror, ctx: Unit?) = error("Unsupported")

        override fun visitError(type: ErrorType, ctx: Unit?) = error("Unsupported")

        override fun visitNoType(type: NoType, ctx: Unit?) = error("Unsupported")

        override fun visitIntersection(type: IntersectionType, ctx: Unit?) = error("Unsupported")

        override fun visitNull(type: NullType, ctx: Unit?) = error("Unsupported")

        override fun visitUnion(type: UnionType, ctx: Unit?) = error("Unsupported")

    }
}
