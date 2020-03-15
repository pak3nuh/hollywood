package pt.pak3nuh.hollywood.processor.generator

import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ElementVisitor
import javax.lang.model.element.Modifier
import javax.lang.model.element.Name
import javax.lang.model.element.NestingKind
import javax.lang.model.element.TypeElement
import javax.lang.model.element.TypeParameterElement
import javax.lang.model.type.TypeMirror

class TypeElementStub(private val kind: ElementKind): TypeElement {
    override fun getModifiers(): MutableSet<Modifier> {
        TODO("Not yet implemented")
    }

    override fun getSimpleName(): Name {
        TODO("Not yet implemented")
    }

    override fun getKind(): ElementKind = kind

    override fun asType(): TypeMirror {
        TODO("Not yet implemented")
    }

    override fun getSuperclass(): TypeMirror {
        TODO("Not yet implemented")
    }

    override fun getTypeParameters(): MutableList<out TypeParameterElement> {
        TODO("Not yet implemented")
    }

    override fun getQualifiedName(): Name {
        TODO("Not yet implemented")
    }

    override fun getEnclosingElement(): Element {
        TODO("Not yet implemented")
    }

    override fun getInterfaces(): MutableList<out TypeMirror> {
        TODO("Not yet implemented")
    }

    override fun <R : Any?, P : Any?> accept(v: ElementVisitor<R, P>?, p: P): R {
        TODO("Not yet implemented")
    }

    override fun <A : Annotation?> getAnnotationsByType(annotationType: Class<A>?): Array<A> {
        TODO("Not yet implemented")
    }

    override fun <A : Annotation?> getAnnotation(annotationType: Class<A>?): A {
        TODO("Not yet implemented")
    }

    override fun getNestingKind(): NestingKind {
        TODO("Not yet implemented")
    }

    override fun getAnnotationMirrors(): MutableList<out AnnotationMirror> {
        TODO("Not yet implemented")
    }

    override fun getEnclosedElements(): MutableList<out Element> {
        TODO("Not yet implemented")
    }
}
