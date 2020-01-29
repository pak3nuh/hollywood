# Expected deliverables

## V0.0.1

- Base client **API**
- Includes gradle multi-module project definition.

## V0.0.2

- Actor system interfaces
  - `FactoryRegistry`
  - `ActorRepository`
  - `ActorSystem`
- Working Poc of instance creation with a DI framework

## V0.1.0

- Annotation processor
  - Validates signatures, just if they are suspendable
  - Creates actor proxy, with just delegate capabilities
  - Creates base actor factory interfaces
- Change PoC to use generated code

## V0.1.1

- Implement the serialization strategies
- Make proxy prototype that works with serialized messages

## V0.1.2

- Implements serialization validations on the code generator
- Change code generation to provide proxies with serialized message comunication

## V0.2.0

- Implement mailboxes
- Implement dispatch and wait mechanics
- Make a prototype with mailbox capabilities

## V0.2.1

- Change code genrator to include mailboxes
- At this point a minimalistic PoC should be working

## TBD

- Error handling
- Actor address and identity
- Factory extensions