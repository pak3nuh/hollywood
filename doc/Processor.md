## Constraints

Only interfaces can be annotated with `@Actor`.
In order to allow class hierarchies, different proxies need to be issued and this increases project
complexity for little (and convoluted) gain.

Although is possible to create actors manually that don't use interfaces but a class hierarchy.

## Proxy structure

Proxy capabilities can be made by interface composition. This leverages the 
kotlin compiler capabilities and forces no type hierarchy.

A base implementation for a proxy can be a class for convenience. It can later
be extracted to an interface if needed.

```kotlin
class GreeterProxy(
    delegate: Greeter,
    config: ProxyConfiguration
) : BaseProxy(),
    UnknownMessageHandler by handleUnknownMessages(config),
    RecoveryHandler by handleErrors(config)
```
