# Annotation processor

The annotation processor exists to ease the pain of the boilerplate around creating the proxies
and factories required to work with the system.

To work with it just annotate an interface with `@Actor` and it will generate code for the proxy
and actor factory base classes. While the proxy usually is fully transparent to the user,
the actor factory can be used as a base interface that contains all the required peaces to
use on the system, leaving the user to write just the factory methods for the actor implementation.

```kotlin
@Actor
interface Greeter {
    suspend fun greet(name: String): String
}

@Generated("2020-03-15T15:18:15.582Z")
interface GreeterBaseFactory : ActorFactory<Greeter, GreeterProxy> {
  override val actorKClass: KClass<Greeter>
    get() = Greeter::class
  override val proxyKClass: KClass<GreeterProxy>
    get() = GreeterProxy::class
  override fun createProxy(delegate: Greeter, config: ProxyConfiguration): GreeterProxy =
      GreeterProxy(delegate, config)
}

@Generated("2020-03-15T15:18:15.582Z")
class GreeterProxy(
  delegate: Greeter,
  config: ProxyConfiguration
) : ActorProxyBase<Greeter>(delegate, config), Greeter {
  // delegate calls
}
```

## Custom actor proxy

For certain cases it may be useful to use a custom proxy, for example, to do additional logging.

It is possible to specify the proxy class on the actor annotation like
```kotlin
@Actor(CustomProxy::class)
interface Greeter {
    suspend fun greet(name: String): String
}

@Generated("2020-03-15T15:18:15.582Z")
class GreeterProxy(
  delegate: Greeter,
  config: ProxyConfiguration
) : CustomProxy<Greeter>(delegate, config), Greeter {
  // delegate calls
}
```

There are some constraints for this, please see [bellow](#Constraints)

## Two generator options

Because this is a `kotlin` source generator on a `java` annotation processor environment there are two sources of information
available to build the classes.

1. `java` Mirror API
2. `kotlin` Metadata annotation

Most of the information required to generate the source files is available in the Mirror API, but there are some caveats.
There isn't enough information to infer nullability of all arguments in the function parameters, and this is where the
Metadata that the `kotlin` compiler emits comes in handy.

When this annotation is present and readable, the processor can inspect a much closer representation of the actor
interface. When it is not available the generator falls back to plain Mirror API and some features are lost. More on
this [bellow](#Constraints)

**NOTE**: The library used to read the metadata is on the official [Kotlin repo](https://github.com/JetBrains/kotlin/tree/master/libraries/kotlinx-metadata/jvm)
but it has not reached the stable state yet. For that reason and because the data it may be unreadable, 
the features provided by the annotation must be considered **optional** and the Mirror API the main source of truth.

Metadata mode can explicitly be disabled by setting a system property:
`hollywood.processor.disable-kotlin-metadata=true`

## Constraints

These constraints reflect the current status of the project and may change in the future.

#### Suspend everywhere

All methods of an actor must be **suspendable**. This is because an actor will work with
mailboxes, serialization, possibly network, and those are inheritably async in nature.

This should be expected because actor communication is message based and the actor model
gives the guarantee that only one message is processed at any time.

Currently `kotlin` doesn't support `suspend` properties, but they can be easily modeled as functions.

#### Only interfaces can be annotated with `@Actor`.

This is because in order to allow class 
hierarchies, different proxies need to be issued and this increases project
complexity for little (and convoluted) gain.
Although is possible to create actors manually that don't use interfaces but a class hierarchy.

#### Nullability support is not ensured
If the `kotlin` compiler metadata isn't available the generator will fallback to the Mirror API. In this mode
the nullability of the parameters and return type can't be obtained and all the actor signatures must be
non null.

While this may be an inconvenience, nullability can be easily avoided with overloads and wrapping types like Optional.

#### Not all `kotlin` types are supported
**Only applies to Mirror API mode**

Because this is a `java` annotation processor, the `kotlin` compiler will issue `elements` native
to the `JDK` ecosystem. This means that the opposite process must exist, transforming `java` 
elements into `kotlin` types.

Since there is no such mechanism provided by the `kotlin` ecosystem, it was chosen to support
only a subset of those translations. This subset tries to provide just the basic blocks needed
without having to create a new type for the interaction.

The subset is in this [file](../processor/processor-core/src/main/kotlin/pt/pak3nuh/hollywood/processor/generator/mirror/TypeConverter.kt)
and they are roughly:
- Primitives
- Arrays
- List, Set, Map

Despite not being the current case, the possibility for inter-process communication is
kept intentionally open.

#### Generic methods and actors not supported

An actor is a very well defined entity and generics would blur its purpose.

Keeping actors free of generics simplifies greatly code generation logic and helps to maintain
a clear image of the actor purpose.

#### Actors can't be used as parameters

One of the design decisions was that was to emulate simple synchronous method calls. This should
remove any need to pass actors around for most cases because they can return values.

If for some reason it is necessary to pass information about a specific actor, then one should
pass it's identifier to obtain the correct actor instance. 

### Custom proxies

Custom proxies are a work in progress and may change or be deprecated in the future.

Any custom proxy must comply with
- Extend ActorProxyBase
- Be an open class
- Expose a compatible constructor that receives a delegate and a config parameter

The only reason why is forced to extend `ActorProxyBase` is because the generated proxies
will use functionalities present in this class. It is safer to extend the class instead of
maintaining some brittle naming contract.

Some of this restriction apply in different phases. Some will break the annotation processor,
others will break on the `kotlin` compilation phase.

## Proxy structure

Proxies are just dumb delegators and will only contain the required connection code to work
with the base proxy.

The separation of concerns isn't done on the proxy itself to allow custom proxies to be flexible.
