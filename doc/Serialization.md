# Serialization

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
