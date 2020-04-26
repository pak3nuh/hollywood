package pt.pak3nuh.hollywood.processor.generator.mirror

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import pt.pak3nuh.hollywood.actor.proxy.ProxyConfiguration
import pt.pak3nuh.hollywood.processor.Actor
import pt.pak3nuh.hollywood.processor.generator.FileGenerator
import pt.pak3nuh.hollywood.processor.generator.MethodResult
import pt.pak3nuh.hollywood.processor.generator.SourceFile
import pt.pak3nuh.hollywood.processor.generator.TypeResult
import pt.pak3nuh.hollywood.processor.generator.context.GenerationContext
import pt.pak3nuh.hollywood.processor.generator.context.generationAnnotation
import pt.pak3nuh.hollywood.processor.generator.mirror.visitor.TypeElementVisitor
import pt.pak3nuh.hollywood.processor.generator.util.proxyName
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

abstract class ProxyClassGenerator: FileGenerator, TypeElementVisitor() {

    final override fun generate(element: TypeElement, context: GenerationContext): SourceFile {
        require(element.kind == ElementKind.INTERFACE) {
            "Actor annotation can only be used on interfaces"
        }
        return visitType(element, context).toSourceFile()
    }

    final override fun visitType(typeElement: TypeElement, context: GenerationContext): TypeResult {
        val actorInterface = typeElement.asType().asTypeName()

        val newClassName = ClassName.bestGuess(proxyName(actorInterface.toString()))
        val parameterizedProxy = getProxyBaseClass(typeElement, context.typeUtil).parameterizedBy(actorInterface)

        val delegateParam = ParameterSpec.builder("delegate", actorInterface).build()
        val configParam = ParameterSpec.builder("config", ProxyConfiguration::class.asTypeName()).build()

        val ctr = FunSpec
                .constructorBuilder()
                .addParameter(delegateParam)
                .addParameter(configParam)
                .build()

        val classBuilder = TypeSpec.classBuilder(newClassName)
                .addAnnotation(context.generationAnnotation())
                .primaryConstructor(ctr)
                .superclass(parameterizedProxy)
                .addSuperclassConstructorParameter(delegateParam.name)
                .addSuperclassConstructorParameter(configParam.name)
                .addSuperinterface(actorInterface)

        val functions = buildFunctions(typeElement, context)
        classBuilder.addFunctions(functions.map { it.funSpec })

        return TypeResult(newClassName, classBuilder.build())
    }

    abstract fun buildFunctions(typeElement: TypeElement, context: GenerationContext): List<MethodResult>

    private fun getProxyBaseClass(e: TypeElement, typeUtil: TypeUtil): ClassName {
        val annotationValue = e.annotationMirrors.asSequence()
                .filter { it.annotationType.toString() == Actor::class.qualifiedName }
                .flatMap { annotation ->
                    typeUtil.getElementValuesWithDefaults(annotation).filterKeys { key ->
                        key.simpleName.contentEquals(Actor::value.name)
                    }.values.asSequence()
                }.first()


        val value = annotationValue.value as TypeMirror
        require(typeUtil.isValidProxy(value)) { "Type $value is not a valid proxy" }

        return ClassName.bestGuess(value.toString())
    }

}