# Code Generation

Once a colleague of mine told me that "almost every problem in software can be solved with code generation and an additional abstraction layer", and I find this to be acurate.

By generating code we get two pros:
1. Fast execution and lower memory footprint
2. Actually get to see what the code is doing (fully debugable)

The devoleper experience is also improved because first a prototype is built by hand, then, once it works, it is generated automatically.

Alternatives are proxy generation with reflection which is slower and consumes more memory or bytecode emission, which can become **very** complicated.

## Kotlin compiler plugin or annotation processor

The first thing to decide is if we need a compiler plugin or an annotation processor. Actually they have tottally different purposes and here is a nice [presentation](https://resources.jetbrains.com/storage/products/kotlinconf2018/slides/5_Writing%20Your%20First%20Kotlin%20Compiler%20Plugin.pdf) and [video](https://www.youtube.com/watch?v=w-GMlaziIyo) about them.

Actually **Jetbrains** is rewriting their [compiler platform](https://youtu.be/0xKTM0A8gdI?t=1232) in order to solve some of the current compiler plugin shortcomings. They will create a portable format, an intermediate representation, that can be compiled to the final multiplatform target (JVM, JS, Native). This means that writing a compiler plugin now will probably be pointless.

Nevertheless, our use case is much more simple than and an annotation processor should do the trick. All we need to do is emit source code (in Kotlin because `suspend`).

Jetbrains already provides [**kapt**](https://kotlinlang.org/docs/reference/kapt.html#generating-kotlin-sources) which already works with **maven** and **gradle** to provide a way of generating kotlin code, with some [examples](https://github.com/Kotlin/kotlin-examples/tree/master/gradle/kotlin-code-generation).

The main difference between generating kotlin souce or java source is that with kotlin we need to place the output in the directory given by `processingEnv.options["kapt.kotlin.generated"]` and we don't have multiple rounds of annotation procesing.

A nice library to generate kotlin source is [kotlin poet](https://square.github.io/kotlinpoet/)

## Actor Proxy

Given some actor **API** like
```kotlin
@Actor
interface OrderManager {
    suspend fun getOrderId(): String
    suspend fun appendProduct(product: ProductLine)
    suspend fun checkout(): Checkout
}
```

## Abstract factory

Every factory should inherit from a common interface, for both type safety and additional metadata:

```kotlin
interface ActorFactory<out T> {
  val factoryOf: KClass<T>
}
```

This metadata can be used to _glue_ together client and generated proxy code. The lookup for a proxy can be derived from the class provided in `factoryOf` against a registry, that can also be generated and discovered via **SPI**. Alternative options include reflection or include more metadata in the `ActorFactory` like the proxy class.

Then a convenience interface can be generated, but not mandatory to use:
```kotlin
@Generated
interface AOrderManagerFactory: ActorFactory<OrderManager> {
    override val factoryOf: KClass<T> = OrderManager::class
}
```