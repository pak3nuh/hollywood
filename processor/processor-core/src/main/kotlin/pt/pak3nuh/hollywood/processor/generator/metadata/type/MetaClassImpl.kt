package pt.pak3nuh.hollywood.processor.generator.metadata.type

import kotlinx.metadata.KmClass

@Suppress("FunctionName")
fun MetaClass(kmClass: KmClass): MetaClass = MetaClassImpl(kmClass)
private class MetaClassImpl(val kmClass: KmClass) : MetaClass {
    override val functions: List<MetaFun> = kmClass.functions.map { MetaFun(it) }
    override val name: String
        get() = kmClass.name

    override fun toString(): String {
        return "Class $name"
    }
}
