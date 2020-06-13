package pt.pak3nuh.hollywood.processor.generator.kpoet

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.WildcardTypeName

interface KPoetVisitor {
    fun visit(typeName: KTypeName)
    fun visit(typeName: KClassName)
    fun visit(typeName: KWildcardTypeName)
    fun visit(typeName: KParameterizedTypeName)
}

sealed class KPoetElement {
    abstract fun accept(visitor: KPoetVisitor)
}
class KTypeName(val typeName: TypeName): KPoetElement() {
    override fun accept(visitor: KPoetVisitor) = visitor.visit(this)
}
class KClassName(val typeName: ClassName): KPoetElement() {
    override fun accept(visitor: KPoetVisitor) = visitor.visit(this)
}
class KWildcardTypeName(val typeName: WildcardTypeName): KPoetElement() {
    override fun accept(visitor: KPoetVisitor) = visitor.visit(this)
}
class KParameterizedTypeName(val typeName: ParameterizedTypeName): KPoetElement() {
    override fun accept(visitor: KPoetVisitor) = visitor.visit(this)
}

fun TypeName.accept(visitor: KPoetVisitor) = when (this) {
    is ParameterizedTypeName -> KParameterizedTypeName(this)
    is WildcardTypeName -> KWildcardTypeName(this)
    is ClassName -> KClassName(this)
    else -> KTypeName(this)
}.accept(visitor)