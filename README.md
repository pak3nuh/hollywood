# Hollywood

A minimalistic type safe actor based framework built with and for Kotlin.

This project leverages several technologies to provide an easier way to handle concurrent programing. It does not intend to be fully compliant with all the requirements of the actor model, just borrows some of it's main ideas.

It is also not a requirement to design against a multi clustered implementation. This is a next level problem that requires some complicated guarantees like ensuring an actor is not created on multiple clusters. The current design should work on a single machine in order to facilitate some of the work required.

**Disclaimer: Just a PoC at this stage**

More info on the research conducted [here](./doc/research/README.md).

## Usage

### Build an actor system

To create an actor system it is as simple as provide at least one actor factory

```kotlin
val actorSystem = SystemBuilder()
        .registerFactory(GreeterFactory())
        .build()
```

### Create actors

Once the system is build is possible to create actor instances
```kotlin
val actor = actorSystem.factoryRepository.createActor(GreeterFactory::class) { factory ->
    factory.createGreeter()
}
actor.sayHello()
```
The object `actor` is a fully working actor proxy.

## Documentation index

For more detailed documentation

- [Processor](./doc/Processor.md)
