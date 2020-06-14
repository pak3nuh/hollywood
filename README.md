# Hollywood

A minimalistic type safe actor based framework built with and for Kotlin.

This project leverages several technologies to provide an easier way to handle concurrent programing. 
It does not intend to be fully compliant with all the requirements of the actor model, just borrows some of it's main ideas.

It is also not a requirement to design against a multi clustered implementation. 
This is a next level problem that requires some complicated guarantees like ensuring an actor is not created on multiple clusters. 
The current design should work on a single machine in order to facilitate some of the work required.

**Disclaimer: Just a PoC at this stage**

More info on the research conducted [here](./doc/research/README.md).

## Usage

### Build an actor system

To create an actor system it is as simple as provide at least one actor factory. 

In order to solve cyclic dependencies, an `actorSystem` parameter is available in the builder
function. Note this parameter hold the final reference that will be released to the user in the future,
but the system isn't fully built at this point. It isn't expected that factories need those
features before the user gets the system.

```kotlin
val dependency = IoC.get("dependency name")
val actorSystem = SystemBuilder()
        .registerFactory(ClinicFactory::class) { actorSystem, buildProperties ->
            ClinicFactoryImpl(dependency)
        }
        .build()
```

### Create actors

First define an actor, annotate it accordingly and implement it:
```kotlin
@Actor
interface Vet {
    suspend fun checkPet(pet: Pet): CheckResult
}

class VetImpl: Vet {
    override suspend fun checkPet(pet: Pet): CheckResult {
        ...
    }
}
```

Once compilation kicks in a proxy class and a factory interface will be generated. Generated type names are the
actor FQCN sufixed by `Proxy` and `BaseFactory`.

Then implement a factory that provides instances of the actor implementation:
```kotlin
interface VetFactory: VetBaseFactory {
    fun createVet(): Vet
}
class VetFactoryImpl: VerFactory {
    override fun createVet(): Vet = VetImpl()
}
```

Once the system is build is possible to create actor instances
```kotlin
val actor = actorSystem.actorManager.getOrCreateActor(VetFactory::class) { factory ->
    factory.createVet()
}
actor.sayHello()
```
The object `actor` is a fully working actor proxy.

### Build properties

For convenience, is possible to register properties that will be available during the build
phase of the actor system. These properties can hold support types for actor factories.

```kotlin
object ClinicActorsProperty : SystemBuilder.Property<ClinicActors>()
val actorSystem = SystemBuilder()
        .withProperty(ClinicActorsProperty) { actorSystem ->
            ClinicActors(actorSystem.actorManager)
        }
        .registerFactory(ClinicFactory::class) { actorSystem, buildProperties ->
            ClinicFactoryImpl(buildProperties[ClinicActorsProperty])
        }
        .build()
```

Please note that the properties are only available until the system is fully built.

## How to use on your project

Currently, there are no public artifacts available, but the project is installable on your local maven
repository by running `gradlew install`.

Then it should be imported into your project as any other dependency like
```kotlin
plugins {
    kotlin("kapt") // for kotlin annotation processing
}

repositories {
    mavenLocal()
}

dependencies {
    kapt("pt.pak3nuh.hollywood.processor:processor-core:$version")
    implementation("pt.pak3nuh.hollywood:builder:$version")
}
```

## Documentation index

For more detailed documentation

- [Actor Lifecycle](doc/ActorLifecycle.md)
- [Processor](doc/Processor.md)
- [Messaging](doc/Messaging.md)
- [Serialization](doc/Serialization.md)
