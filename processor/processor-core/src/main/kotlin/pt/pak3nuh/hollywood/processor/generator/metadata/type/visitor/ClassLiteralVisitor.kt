package pt.pak3nuh.hollywood.processor.generator.metadata.type.visitor

import com.squareup.kotlinpoet.TypeName
import pt.pak3nuh.hollywood.processor.generator.kpoet.KClassName
import pt.pak3nuh.hollywood.processor.generator.kpoet.KParameterizedTypeName
import pt.pak3nuh.hollywood.processor.generator.kpoet.KPoetVisitor
import pt.pak3nuh.hollywood.processor.generator.kpoet.KTypeName
import pt.pak3nuh.hollywood.processor.generator.kpoet.KWildcardTypeName
import pt.pak3nuh.hollywood.processor.generator.kpoet.isArray

class ClassLiteralVisitor: KPoetVisitor {

    lateinit var result: TypeName

    override fun visit(typeName: KTypeName) {
        result = typeName.typeName.copy(nullable = false)
    }

    override fun visit(typeName: KClassName) {
        result = typeName.typeName.copy(nullable = false)
    }

    override fun visit(typeName: KWildcardTypeName) {
        throw UnsupportedOperationException()
    }

    override fun visit(typeName: KParameterizedTypeName) {
        // arrays are reified
        // type literals in kotlin must not be nullable and generics not reified
        if (typeName.typeName.isArray()) {
            result = typeName.typeName.copy(nullable = false)
        } else {
            visit(KTypeName(typeName.typeName.rawType))
        }
    }
}
