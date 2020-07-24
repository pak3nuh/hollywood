# Serialization

Serialization is the process of transforming a `in-memory` representation of data into bytes. In this project, there are
3 different approaches for serialization of messages and responses.

### Kotlin serializer

The library has first class support for classes annotated with `kotlinx.serialization.Serializable`. When the
Kotlin serialization plugin is active, the compiler generates AST code for the class definitions.

In this project we can leverage this AST structures to allow for function signatures with `@Serializable` parameters
and return values. In the compile phase, an annotation processor is registered to generate a file that will expose
all the serialization ready classes through an [SPI](../processor/processor-api/src/main/kotlin/pt/pak3nuh/hollywood/processor/api/Serializers.kt).

The generated bridge class will always have the same name, **so it may generate split package problems if several jar files
with a generated bridge are used.**

A user can very easily extend the support for more classes by creating a new class that implements the SPI interface and
register it as a service in the `META-INF/services` folder.

### Externalizable serializer

This is the serializer that will provide the most flexibility of them all. It should be used when special care must be
taken on serialization of certain classes.

To be enable this serializer, a class must implement the `java.io.Externalizable` interface.

This is also the only serializer that will process responses with a fixed structure, like UNIT and EXCEPTION cases.
It would be a waste of effort to support them on all the serializers. 

### Default serializer

This serializer acts as a fallback for all others. It is designed to work in the most flexible
way possible, but it has it's drawbacks.

This serializer is designed to work with types you don't own and has fewer limitations on 
what it can't handle. It can:
- Serialize objects not marked with `java.io.Serializable` or similar
- Instantiate objects without a no args constructor
- Write to fields without a setter

In order to be this flexible it can use heavily reflection, ASM and even non portable apis,
so this should be used carefully.

Although these features are available, it will prefer to use simple reflection before getting
creative.

Needless to say, this is the least performant of all serializers.

#### Object definitions

**Special attention** should be placed on serializing _object definitions_. Since this are
kotlin constructs, some serializers will attempt to reproduce the same structure, but
will not be the same instance. Enums shall be used instead on _object definitions_.

## Serializer selection process

In theory a message or a response can be serializer by all serializers at the same time.
The selection process is ordered to allow the some degree of determinism:

1. Externalizable
2. Kotlin Serializable
3. Default

The selection process occurs once for each message or response, being possible to use on serializer on a message, and another
on the response.

For a message to be eligible for a serializer, all the message's parameters must pass the serializer test.