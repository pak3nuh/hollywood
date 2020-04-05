package pt.pak3nuh.hollywood.processor.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import pt.pak3nuh.hollywood.actor.proxy.ProxyConfiguration
import pt.pak3nuh.hollywood.processor.Actor
import pt.pak3nuh.hollywood.processor.generator.context.GenerationContext
import pt.pak3nuh.hollywood.processor.generator.context.generationAnnotation
import pt.pak3nuh.hollywood.processor.generator.types.KotlinMetadata
import pt.pak3nuh.hollywood.processor.generator.types.TypeUtil
import pt.pak3nuh.hollywood.processor.visitor.TypeElementVisitor
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

class ActorProxyGenerator(
        private val methodGenerator: MethodVisitor,
        private val metadataExtractor: (TypeElement) -> KotlinMetadata
) : FileGenerator, TypeElementVisitor() {

    override fun generate(element: TypeElement, context: GenerationContext): SourceFile {
        require(element.kind == ElementKind.INTERFACE) {
            "Actor annotation can only be used on interfaces"
        }
        context[KotlinMetadata] = metadataExtractor(element)
        val sourceFile = visitType(element, context).toSourceFile()
        context.remove(KotlinMetadata)
        return sourceFile
    }

    override fun visitType(typeElement: TypeElement, context: GenerationContext): TypeResult {
        val actorInterface = typeElement.asType().asTypeName()

        val newClassName = ClassName.bestGuess("${actorInterface}Proxy")
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

        typeElement.enclosedElements.asSequence()
                .map { it.accept(methodGenerator, context) }
                .filterIsInstance<MethodResult>()
                .forEach {
                    classBuilder.addFunction(it.funSpec)
                }

        return TypeResult(newClassName, classBuilder.build())
    }

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

