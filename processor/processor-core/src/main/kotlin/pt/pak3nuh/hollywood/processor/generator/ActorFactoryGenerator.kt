package pt.pak3nuh.hollywood.processor.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import pt.pak3nuh.hollywood.actor.ActorFactory
import pt.pak3nuh.hollywood.actor.proxy.ProxyConfiguration
import pt.pak3nuh.hollywood.processor.generator.context.GenerationAnnotation.buildGenerationAnnotation
import pt.pak3nuh.hollywood.processor.generator.context.GenerationContext
import pt.pak3nuh.hollywood.processor.visitor.TypeElementVisitor
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import kotlin.reflect.KClass

class ActorFactoryGenerator(
        private val element: Element,
        private val ctx: GenerationContext
) : Generator, TypeElementVisitor() {

    override fun generate(): SourceFile {
        val result = element.accept(this, ctx) as? TypeResult ?: error("Result is not a class")
        return result.toSourceFile()
    }

    override fun visitType(e: TypeElement, context: GenerationContext): Result {
        val actorInterface = e.asType().asTypeName()
        val proxyName = ClassName.bestGuess("${actorInterface}Proxy")
        val factoryName = ClassName.bestGuess("${actorInterface}BaseFactory")

        val parameterizedFactory = actorFactoryInterface.parameterizedBy(actorInterface, proxyName)

        val actorKClassProp = buildProp(actorInterface, "actorKClass")
        val proxyKClassProp = buildProp(proxyName, "proxyKClass")

        val proxyCreator = FunSpec.builder("createProxy")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter("delegate", actorInterface)
                .addParameter("config", proxyConfigInterface)
                .returns(proxyName)
                .addStatement("return %T(delegate, config.actorId)", proxyName)
                .build()

        val classBuilder = TypeSpec.interfaceBuilder(factoryName)
                .addAnnotation(ctx.buildGenerationAnnotation())
                .addProperty(actorKClassProp)
                .addProperty(proxyKClassProp)
                .addFunction(proxyCreator)
                .addSuperinterface(parameterizedFactory)

        return TypeResult(factoryName, classBuilder.build())
    }

    private fun buildProp(kClassParam: TypeName, name: String): PropertySpec {
        val kClassType = KClass::class.asTypeName()
        return PropertySpec.builder(name, kClassType.parameterizedBy(kClassParam))
                .addModifiers(KModifier.OVERRIDE)
                .getter(FunSpec.getterBuilder().addCode("return %T::class", kClassParam).build())
                .build()
    }

    private companion object {
        val actorFactoryInterface = ActorFactory::class.asClassName()
        val proxyConfigInterface = ProxyConfiguration::class.asClassName()
    }
}

