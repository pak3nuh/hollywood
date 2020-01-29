# Type safe Actors

One of the shortcoming of current actor frameworks is the messaging mechanism used. For instance is possible to send unknown messages to an actor or messaging must comply to a user defined response/reply protocol.

I would like to have a perfectly safe API to work with like:

```kotlin
@Actor
interface Greeter {
  suspend fun greet(): String
}

suspend fun greetFromActor(actor: Greeter) {
	println(actor.greet())
}
```

From a consumer perspective, I would like to be oblivious to what is happening under the hood to talk to the `Greeter` instance, just write simple and understandable code.

## Proposal

To define an actor, one just needs to create an interface annotated with `@Actor`. This annotation serves as discovery mechanism to generate the actor proxies and the abstract factories. Interfaces are also very useful because they are composable and don't restrict class hierarchies. For example and actor may implement a handler for unknown messages.

```kotlin
interface UnknownMessageHandler {
	suspend fun onUnknownMessage(message: Message): HandlingResult
}
@Actor
interface Greeter: { ... } // shouldn't leak to actor API
class GreeterImpl: Greeter, UnknownMessageHandler { ... }
```
The framework code can pickup this composable interfaces and generate a proxy with these `extension` capabilities. Other capabilities may be exception recovery or persistent state. More on this bellow.

To avoid threads being blocked, all the method on the actor API must be defined as `suspending`. A similar approach has been chosen by Orleans, only allowing `Task`s.

### Actor creation

One of the pain  points in my attempts to implement a working system with actors was dependency management or trying to implement any kind of IoC. There are some constraints that I would like to keep in mind:
1. Should be easy to plugin any kind of IoC container.
2. The actor creation must be as type safe as the actor usage.
3. Actor life cycle is fully managed by the user, at least for now. May include some goodies like actor hierarchies, ie when one dies, all its children dies.

To tackle point 1, factories must be provided to the framework, so that they can contain any DI container.
```kotlin
interface GreeterFactory {
	fun create(param1: String): Greeter
}
class GreeterFactoryImpl(context: ApplicationContext): GreeterFactory {
	override fun create(param1: String): Greeter = GreeterImpl(context.getBean<Repository>(), param1)
}
```
So the wiring is likely to be manual, something like:
```kotlin
interface FactoryRegistry {
  fun <F: ActorFactory<T>, I: F> register(factoryInstance: I, factoryInterface: KClass<F>)
}
fun registerMyFactory(factory: GreeterFactoryImpl) = registry.register(factory, GreeterFactory::class)
```
This also works to solve item 2, type safe factories:
```kotlin
interface ActorRepository {
  fun <F> factory(factoryInterface: KClass<F>, actorId: String = null): F
}
fun createGreeter(): Greeter = repository.factory<GreeterFactory>("myId").create("Arthur Pendragon")
```
Also some boilerplate can be generated to improve the experience a bit, like base interfaces for factories. The final API would be close to this:
```kotlin
@Actor
interface Greeter {
  suspend fun greet(): String
}

@Generated
class GreeterProxy(val delegate: Greeter): BaseActorProxy(), Greeter {
  override suspend fun greet(): String {
    // prepare call and other stuff
    return callDelegate {
      delegate.greet()
    }
  }
}

interface ActorFactory<out T> {
  val factoryOf: KClass<T>
}

@Generated
interface AGreeterFactory: ActorFactory<Greeter> {
  override val factoryOf = Greeter::class
}

interface GreeterFactory: AGreeterFactory {
	fun create(param1: String): Greeter
}

interface ActorRepository {
  fun <T, F: ActorFactory<T>, I: F> registerFactory(factoryInstance: I, factoryInterface: KClass<F>)
  fun <T, F: ActorFactory<T>> factory(factoryInterface: KClass<F>, actorId: String = null): F
  fun <T> find(actorId: String, KClass<T>): T?
}
```
Instances should be optionally named to make it easy to scope state. Ex: all the actors for a specific order should contain the order id.

The factories itself could have the capability to extend an actor, instead of just applying predefined interfaces, something like:
```kotlin
interface ActorRepository {
	...
	fun <T, F: ActorFactory<T>> extendedFactory(factoryInterface: KClass<F>)
}
interface FactoryExtender<out T, out F: ActorFactory<T>> {
	fun withUknownMessageHandler(handler: UnknownMessageHandler): FactoryExtender<T, F>
	fun get(): F
}
```
This customizes actor instances, instead of definitions, and does not leak framework concepts to the actor API.