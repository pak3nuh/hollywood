# Actor Lifecycle

Actors need to live as long as anyone can reach and talk to them. Because all actors will
live inside the same JVM instance, we can leverage the GC the ability to collect objects
that no longer are accessible.

This comes with an additional framework overhead because it can't hold to any direct or
indirect references to actors, and must use 
[Reference](https://docs.oracle.com/javase/8/docs/api/java/lang/ref/Reference.html) types.

This also means that at some point in the future, disposal callbacks need to run, due to
internals of the technologies planned to be used, and because an actor can't be self closeable.

### A practical example

We receive an HTTP request to place an item in the basket with ID `123`.
First we need to obtain the actor for basket `123`.

Since the actor is not being held by anyone, then it needs to be created and bootstrapped
(internal state restored) before being delivered.

Now another identical request is received while the first is still being processed. The basket
actor still lives in memory, so can just be safely obtained and interacted with.

When all the requests are served, the actor may be reclaimed by the GC.

## Force dispose

An actor may be disposed forcefully instead of automatically reclaimed. This may be dangerous
since there may exist active references for an actor.

This may be useful to cancel any in flight requests, but will only actually have some effect
on future version of the communication infrastructure.

## Actor coroutine scope

All actors inside the system run inside a common scope. This scope is tied to the actor system itself and is not
shared between system instances. Whenever anyone needs to access the scope, it is available externally inside
[ActorSystem](../api/src/main/kotlin/pt/pak3nuh/hollywood/system/ActorSystem.kt) interface.

This scope starts on a supervisor job, that allows for children coroutine failure without parent failure, but for
the time being, if an unhandled exception bubbles to the actor system, it shuts down the scope.

Some basic configuration options are exposed, like the number of threads available for coroutine handing. 
