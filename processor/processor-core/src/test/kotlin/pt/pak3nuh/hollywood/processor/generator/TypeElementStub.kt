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
        unsupported()
    }

    override fun getSimpleName(): Name {
        unsupported()
    }

    override fun getKind(): ElementKind = kind

    override fun asType(): TypeMirror {
        unsupported()
    }

    override fun getSuperclass(): TypeMirror {
        unsupported()
    }

    override fun getTypeParameters(): MutableList<out TypeParameterElement> {
        unsupported()
    }

    override fun getQualifiedName(): Name {
        unsupported()
    }

    override fun getEnclosingElement(): Element {
        unsupported()
    }

    override fun getInterfaces(): MutableList<out TypeMirror> {
        unsupported()
    }

    override fun <R : Any?, P : Any?> accept(v: ElementVisitor<R, P>?, p: P): R {
        unsupported()
    }

    override fun <A : Annotation?> getAnnotationsByType(annotationType: Class<A>?): Array<A> {
        unsupported()
    }

    override fun <A : Annotation?> getAnnotation(annotationType: Class<A>?): A {
        unsupported()
    }

    override fun getNestingKind(): NestingKind {
        unsupported()
    }

    override fun getAnnotationMirrors(): MutableList<out AnnotationMirror> {
        unsupported()
    }

    override fun getEnclosedElements(): MutableList<out Element> {
        unsupported()
    }
    
    private fun unsupported(): Nothing = throw UnsupportedOperationException()
}
