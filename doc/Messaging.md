# Messaging

Actor communication is based on messages. This approach enables actors to react to messages
originated from other processes and forbids shared memory. Shared memory is the reason why
concurrency constructs exist, to control access to those resources.

By having messages that are plain data holders, it is easy to enforce these constraints.
For the purpose of this framework a generic message approach was taken. This message format
represents a function call and contains a functionId, which must be constant for the same
function signature, allowing additions to the actor structure, but not modifications, and
its parameters stored in a positional basis. Additional metadata like parameter names, return
types may also be added for validation and debug purposes.

**It's not in scope to evaluate if a message is only a data holder or not.**

### Message passing

Messages will be passed to other actors using the mailbox concept. An actor life will be an
endless loop of picking up the first message in the mailbox, unpack it and execute the correct
function.

Messages will live in memory in their plain object form, because are easier to develop and
debug, but will cross actor boundaries in a serialized format for two main reasons:
1. Enforces usage of data holders
1. Keeps open communication between machines and actor state persistence

### Message structure

Each message should encapsulate the minimum set of information required to make a remote
function call. It should be:
- The function id (resistant to changes like reordering, but not schema changes)
- The function parameters in the order they are defined

#### Function ids

A function id should have almost the same semantics of a function signature. Within the context of
the actor system, it should identify uniquely a function defined within an actor interface.

The actual details of that specification aren't public by design, to maintain some flexibility
by the library maintainers, but it's contract should be enforced by entities that build proxies 
(being automatic or custom), and the entities that create 
[Messages](../api/src/main/kotlin/pt/pak3nuh/hollywood/actor/message/Message.kt) instances.

For details of that contract, check [this class](../core/src/main/kotlin/pt/pak3nuh/hollywood/system/actor/message/FunctionSignatureBuilder.kt). 
Be aware relying on it can cause breaking changes between versions.

### Message versioning

Messages should have a version, mainly for schema changes or cross machine clusters.
The version support will only be implemented when and if state persistence is allowed.

### Unknown messages handling

There is a possibility of receiving an unknown message, again in the same situations listed
[above](#Message versioning). When this is possible, an handler should exist to react to
these messages.
