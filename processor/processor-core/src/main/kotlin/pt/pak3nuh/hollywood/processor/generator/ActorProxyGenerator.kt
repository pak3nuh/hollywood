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
import pt.pak3nuh.hollywood.processor.visitor.MethodGenerator
import pt.pak3nuh.hollywood.processor.visitor.TypeElementVisitor
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements

class ActorProxyGenerator(
        private val elements: Elements,
        private val methodGenerator: MethodGenerator
) : FileGenerator, TypeElementVisitor() {

    override fun generate(element: TypeElement, context: GenerationContext): SourceFile {
        return visitType(element, context).toSourceFile()
    }

    override fun visitType(typeElement: TypeElement, context: GenerationContext): TypeResult {
        val actorInterface = typeElement.asType().asTypeName()

        val newClassName = ClassName.bestGuess("${actorInterface}Proxy")
        val parameterizedProxy = getProxyBaseClass(typeElement).parameterizedBy(actorInterface)

        val delegateParam = ParameterSpec.builder("delegate", actorInterface).build()
        val configParam = ParameterSpec.builder("config", ProxyConfiguration::class.asTypeName()).build()

        val ctr = FunSpec
                .constructorBuilder()
                .addParameter(delegateParam)
                .addParameter(configParam)
                .build()

        val classBuilder = TypeSpec.classBuilder(newClassName)
                .addAnnotation(context.buildGenerationAnnotation())
                .primaryConstructor(ctr)
                .superclass(parameterizedProxy)
                .addSuperclassConstructorParameter(delegateParam.name)
                .addSuperclassConstructorParameter(configParam.name)
                .addSuperinterface(actorInterface)

        typeElement.enclosedElements.asSequence()
                .map { it.accept(methodGenerator, context) }
                .filterIsInstance<MethodResult>()
                .forEach {
                    classBuilder.addFunction(it.funSpec)
                }

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

