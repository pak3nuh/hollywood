package pt.pak3nuh.hollywood.processor.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import pt.pak3nuh.hollywood.actor.proxy.ProxyConfiguration
import pt.pak3nuh.hollywood.processor.Actor
import pt.pak3nuh.hollywood.processor.generator.context.GenerationAnnotation.buildGenerationAnnotation
import pt.pak3nuh.hollywood.processor.generator.context.GenerationContext
import pt.pak3nuh.hollywood.processor.visitor.TypeElementVisitor
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements

// todo refactor this class to play better with the visitor or drop it alltogether
class ActorProxyGenerator(
        private val element: Element,
        private val elements: Elements,
        private val ctx: GenerationContext
) : Generator, TypeElementVisitor() {

    override fun generate(): SourceFile {
        val result = element.accept(this, ctx) as? TypeResult ?: error("Result is not a class")
        return result.toSourceFile()
    }

    override fun visitType(e: TypeElement, context: GenerationContext): Result {
        val actorInterface = e.asType().asTypeName()

        val newClassName = ClassName.bestGuess("${actorInterface}Proxy")
        val parameterizedProxy = getProxyBaseClass(e).parameterizedBy(actorInterface)

        val delegateParam = ParameterSpec.builder("delegate", actorInterface).build()
        val configParam = ParameterSpec.builder("config", ProxyConfiguration::class.asTypeName()).build()

        val ctr = FunSpec
                .constructorBuilder()
                .addParameter(delegateParam)
                .addParameter(configParam)
                .build()

        val classBuilder = TypeSpec.classBuilder(newClassName)
                .addAnnotation(ctx.buildGenerationAnnotation())
                .primaryConstructor(ctr)
                .superclass(parameterizedProxy)
                .addSuperclassConstructorParameter(delegateParam.name)
                .addSuperclassConstructorParameter(configParam.name)
                .addSuperinterface(actorInterface, "delegate")

        return TypeResult(newClassName, classBuilder.build())
    }

    private fun getProxyBaseClass(e: TypeElement): ClassName {
        val annotationValue = e.annotationMirrors.asSequence()
                .filter { it.annotationType.toString() == Actor::class.qualifiedName }
                .flatMap { annotation ->
                    elements.getElementValuesWithDefaults(annotation).filterKeys { key ->
                        key.simpleName.contentEquals(Actor::value.name)
                    }.values.asSequence()
                }.first()
        return ClassName.bestGuess(annotationValue.value.toString())
    }
}

