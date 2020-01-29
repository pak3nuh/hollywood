# Persist Actor State

**Not intended to be addressed on this PoC**

As with any system, it is impossible to keep it running forever. This is not just because of external things like power outages or other unforeseeable events, but also due to planed maintenance or bug fixing.

Depending on the ecosystem as a whole, actor state persistence can become very important.

Say, for example, you are doing some kind of operation that requires you to synchronize multiple external services, much like a distributed transaction. Obviously there are patterns that help with this, but just stick with me.

Now lets assume you are in the middle of an operation and one of your machines goes down. With patterns like [Saga](https://microservices.io/patterns/data/saga.html) this will typically mean you need to rollback the current operation to the next valid state.

If all your messaging is serializable, your actor state is just the replay of all messages up to a certain point. This is very similar to [event sourcing](https://martinfowler.com/eaaDev/EventSourcing.html) architecture.

This is just scratching the surface and there are problems with this approach. We can't replay messages that trigger communication with another actors, but, if an actor's internal state change is only done with special messages, and not with side effects of other messages, this should be possible by serializing the mailbox to a persistent medium.

## Message versioning

Once you start persisting state, you have another problem, that it breaking schema changes.

Any active software continuously changes its internals by adding new features or fixing bugs, and components that hold state also change.

In order to allow for schema changes, all messaging must be versioned and comply to a migration path. Such migration can be made offline, migration specific tooling, or online, providing the actor system functions to migrate each message.

