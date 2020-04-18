package pt.pak3nuh.hollywood.processor.generator.metadata.type

import kotlinx.metadata.Flag
import kotlinx.metadata.KmFunction

@Suppress("FunctionName")
fun MetaFun(kmFunction: KmFunction): MetaFun = MetaFunImpl(kmFunction)

private class MetaFunImpl(val kmFunction: KmFunction) : MetaFun {
    override val parameters: List<MetaParameter> = kmFunction.valueParameters
            .map { MetaParameter(it, kmFunction.typeParameters) }

    override val isSuspend: Boolean
        get() = Flag.Function.IS_SUSPEND(kmFunction.flags)

    override val name: String
        get() = kmFunction.name

    override val returnType: MetaType = MetaType(kmFunction.returnType, kmFunction.typeParameters)
}
