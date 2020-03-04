package pt.pak3nuh.hollywood.processor.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import pt.pak3nuh.hollywood.processor.Generated
import pt.pak3nuh.hollywood.processor.generator.context.GenerationAnnotation
import pt.pak3nuh.hollywood.processor.generator.context.GenerationContext
import pt.pak3nuh.hollywood.processor.visitor.TypeElementVisitor
import java.time.Instant
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

class ActorProxyGenerator(private val element: Element, private val ctx: GenerationContext) : Generator, TypeElementVisitor() {

    override fun generate(): SourceFile {
        val result = element.accept(this, ctx) as? TypeResult ?: error("Result is not a class")
        return result.toSourceFile()
    }

    override fun visitType(e: TypeElement, context: GenerationContext): Result {
        val actorInterface = e.asType().asTypeName()
        val newClassName = ClassName.bestGuess("${actorInterface}Proxy")

        val parameterizedProxy = actorProxyInterface.parameterizedBy(actorInterface)

        val (delegateParam, delegateProperty) = ctrPropertyPair("delegate", actorInterface)
        val (actorIdParam ,actorIdProperty) = ctrPropertyPair("actorId", String::class.asTypeName())

        val ctr = FunSpec
                .constructorBuilder()
                .addParameter(delegateParam)
                .addParameter(actorIdParam)
                .build()

        val classBuilder = TypeSpec.classBuilder(newClassName)
                .addAnnotation(ctx.getGenerationAnnotation())
                .primaryConstructor(ctr)
                .addProperty(delegateProperty)
                .addProperty(actorIdProperty)
                .addSuperinterface(parameterizedProxy)
                .addSuperinterface(actorInterface, "delegate")

        return TypeResult(newClassName, classBuilder.build())
    }

    private fun ctrPropertyPair(name: String, actorInterface: TypeName): Pair<ParameterSpec, PropertySpec> {
        val delegateParam = ParameterSpec.builder(name, actorInterface)
                .build()
        val delegateProperty = PropertySpec.builder(name, actorInterface)
                .addModifiers(KModifier.OVERRIDE)
                .initializer(name)
                .build()
        return Pair(delegateParam, delegateProperty)
    }

    private fun GenerationContext.getGenerationAnnotation(): AnnotationSpec {
        val existing = get(GenerationAnnotation)
        if (existing != null) {
            return existing
        }

        val generatedAnnotation = AnnotationSpec.builder(Generated::class)
                .addMember("%S", Instant.now())
                .build()

        set(GenerationAnnotation, generatedAnnotation)
        return generatedAnnotation
    }

    private companion object {
        val actorProxyInterface: ClassName = ClassName.bestGuess("pt.pak3nuh.hollywood.actor.proxy.ActorProxy")
    }
}

