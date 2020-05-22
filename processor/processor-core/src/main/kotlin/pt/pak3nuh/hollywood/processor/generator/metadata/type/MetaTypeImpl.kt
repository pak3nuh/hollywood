package pt.pak3nuh.hollywood.processor.generator.metadata.type

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.WildcardTypeName
import kotlinx.metadata.Flag
import kotlinx.metadata.Flags
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.KmType
import kotlinx.metadata.KmTypeParameter
import kotlinx.metadata.KmTypeVisitor
import kotlinx.metadata.KmVariance
import kotlinx.metadata.ClassName as KClassName

@Suppress("FunctionName")
fun MetaType(
        kmType: KmType,
        typeParameters: List<KmTypeParameter>
): MetaType = MetaTypeImpl(kmType, typeParameters)

class MetaTypeImpl(
        private val kmType: KmType,
        private val typeParameters: List<KmTypeParameter>
) : MetaType {
    override val name: String = getTypeName()

    private fun getTypeName(): String {
        return when (val classifier = kmType.classifier) {
            is KmClassifier.Class -> classifier.name.replace('/','.')
            is KmClassifier.TypeAlias -> classifier.name.replace('/','.')
            is KmClassifier.TypeParameter -> typeParameters.first { it.id == classifier.id }.name
        }
    }

    override val isArray: Boolean
        get() {
            return when (val classifier = kmType.classifier) {
                is KmClassifier.Class -> {
                    // todo improve
                    Regex("kotlin\\.(.*)Array").matches(classifier.name)
                }
                else -> false
            }
        }

    override fun asTypeName(): TypeName {
        return TypeVisitor(kmType.flags, KmVariance.INVARIANT, null).also(kmType::accept).typeName
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is MetaType) {
            return false
        }
        return other.name == name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

}

class TypeVisitor(
        private val flags: Flags,
        private val variance: KmVariance,
        private val parent: TypeVisitor?
) : KmTypeVisitor() {

    lateinit var typeName: TypeName
        private set

    override fun visitClass(kName: KClassName) {
        // / -> .
        // . -> annonymous classes, nor supported
        val name = kName.replace('/', '.')
        typeName = ClassName.bestGuess(name)
    }

    override fun visitStarProjection() {
        parameterize(STAR)
    }

    private fun onChildEnd(child: TypeName) {
        parameterize(child)
    }

    private fun parameterize(child: TypeName) {
        typeName = when (val t = typeName) {
            is ClassName -> t.parameterizedBy(child)
            is ParameterizedTypeName -> t.plusParameter(child)
            else -> error("Unsupported")
        }
    }

    override fun visitEnd() {
        var baseType = typeName
        if (Flag.Type.IS_NULLABLE(flags)) {
            baseType = baseType.copy(nullable = true)
        }
        baseType = when (variance) {
            KmVariance.INVARIANT -> baseType
            KmVariance.IN -> WildcardTypeName.consumerOf(baseType)
            KmVariance.OUT -> WildcardTypeName.producerOf(baseType)
        }
        typeName = baseType
        parent?.onChildEnd(baseType)
    }

    override fun visitArgument(flags: Flags, variance: KmVariance): KmTypeVisitor? {
        return TypeVisitor(flags, variance, this)
    }

    override fun visitTypeParameter(id: Int) {
        error("Type parameters not supported")
    }
}
