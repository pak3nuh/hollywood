package pt.pak3nuh.hollywood.processor.generator

import com.squareup.kotlinpoet.TypeSpec
import pt.pak3nuh.hollywood.processor.visitor.TypeElementVisitor
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

class ActorProxyGenerator(private val element: Element) : Generator, TypeElementVisitor() {

    override fun generate(): SourceFile {
        val result = element.accept(this, Unit) as? TypeResult ?: error("Result is not a class")
        return result.toSourceFile()
    }

    override fun visitType(e: TypeElement, context: Unit): Result {
        val className = "${e.qualifiedName}Proxy"
        val classBuilder = TypeSpec.classBuilder(className)
        return TypeResult(classBuilder.build())
    }

}

