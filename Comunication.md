# Comunication infrastructure

Actual comunication between actor could be a mix of several technologies. 

- Kotlin serialization for generated complex DTO or something like MessagePack
- Kotlin channels for mailbox implementation
- Kotlin suspending functions for message passing and concurrency handling. By only allowing suspending API it is possible to comunicate seamlessly between actors.

## Message serialization

Message passing (materialized as methods) between actors should be a transparent operation. A user should not be aware of special rules regarding message/method definition.

Although it is usefull to define a set of (relaxed) rules to define messaging:

- All messages must be suspending (see [Actors](./Actors.md))
- All parameters must implement a serialization strategy
  - Primitives are inheritably serializable
  - Implement [`java.io.Externalizable`](https://docs.oracle.com/javase/7/docs/api/java/io/Externalizable.html) interface. Provides customization.
  - Annotated with [`kotlinx.serialization.Serializable`](#@Serializable). Provides speed, but only work with project types.
  - If none of the above, defaults to [**Messagepack**](#Messagepack). Provides flexibility for non project types.

In the first versions, it is likely that all message's parameters must comply to at least one of the above strategies. Later implementations may allow multiplexing of strategies, probably at the cost of efficiency.

The strategy resolution is from top to bottom to allow custom implemenations with `Externalizable`.

As a downside, it is possible to change the serialization strategy without knowing. But since all strategies are for a different use case, it is assumed the user is carefull with this.

A serialization protocol is being considered, instead of shared memory, in order to keep open the possibility of state persistence and cross machine boundaries.

### @Serializable

This is a relatively new kotlin sub-project and it aims to provide fast and type safe serialization by bringing code generation to the table.

The main idea is, one marks a class with `@Serializable` and the kotlin compiler will emit a custom **AST** specialized to navigate the class definition. A user needs only to implement the parser _et voila_.

This is specialy powerful if we consider that almost all **IPC** is serialized somehow. **REST** apis, **Databases**, **TCP**, they all work with a form of serialization, and with the same **AST** we can transform that data into another format easily.

### Messagepack

The main idea here is flexibility, by having a non intrusive, binary format, that works with types that are out of our control.

Being a [specification](https://github.com/msgpack/msgpack/blob/master/spec.md) it provides a lot of flexibility to the implementrs. There are loads of libraries that can produce **Messagepack** payloads.

A native kotlin implementation of this spec is [Moshi](https://github.com/square/moshi). This is a good candidade because is kotlin only and is not optimized for **Java** users and is maintained by **Square**, a **Google** subsidiary.

## Mailboxes

Actor systems tipicaly have a mailbox concept. It is a component that holds messages, preserving it's order, until the actor can process them.

This concept is crutial for the concurrency model underneath, since it isn't alowed more than one **thread** hold an actor. Each message is processed on a serial fashion, to ensure a consistent state without race conditions.

### Channels

Once again, Kotlin provides a nice concept to model this, [Channels](https://kotlinlang.org/docs/reference/coroutines/channels.html). At heart a channel is a queue with suspendable blocking semantics.

A channel can be a `SendChannel<E>`
```kotlin
public suspend fun send(element: E)
```
a `ReceiveChannel<E>`
```kotlin
public suspend fun receive(): E
```
 or both.
 
 There are a few implementations of channels with different semantics. For example a [RendezvousChannel](https://github.com/Kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/common/src/channels/RendezvousChannel.kt) will block both sender and receiver until they both invoke their respecive operations, while a [LinkedListChannel](https://github.com/Kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/common/src/channels/LinkedListChannel.kt) will offer an unbounded buffer, never blocking the sender.

Since we are aiming to simulate a single threaded model, something like **RendezvousChannel** is needed with a bit of pixie dust. For example, we don't want to return the suspending send call as soon as the message is received by the other end, but as soon as it is processed.

### Kotlin actors

Kotlin already provides an [Actor](https://github.com/Kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/src/channels/Actor.kt) implementation, which already uses channels in the background. By using this **API** with a generic message parameter.

This approach may already contain the base comunication infrstructure needed, like blocking until the message is processed and consumer mesages in a serialized fashion.

### Futures

Another way of looking at the problem is with futures (or [Deferred](https://github.com/Kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/common/src/Deferred.kt)). With a thin layer to adapt a suspending call, it is possible to use futures to encapsulate the async nature of the call.

This is a fallback in case the channels approach proves too complex or not a good fit.