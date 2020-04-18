package pt.pak3nuh.hollywood.processor.generator.metadata.type

import kotlinx.metadata.KmTypeParameter
import kotlinx.metadata.KmValueParameter

@Suppress("FunctionName")
fun MetaParameter(
        valueParameter: KmValueParameter,
        typeParameters: List<KmTypeParameter>
): MetaParameter = MetaParameterImpl(valueParameter, typeParameters)

private class MetaParameterImpl(
        val parameter: KmValueParameter,
        val typeParameters: List<KmTypeParameter>
) : MetaParameter {
    override val name: String
        get() = parameter.name
    override val type: MetaType = findType()

    private fun findType(): MetaType {
        val kmType = parameter.type ?: parameter.varargElementType ?: error("Parameter $parameter must have a type!")
        return MetaType(kmType, typeParameters)
    }
}
