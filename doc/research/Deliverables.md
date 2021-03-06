# Expected deliverables

## V0.0.1

- Base client **API**
- Includes gradle multi-module project definition.
- Actor system interfaces
  - `FactoryRegistry`
  - `ActorRepository`
  - `ActorSystem`

## V0.0.2

- Working Poc of instance creation with DI in place
  - Proxies talk directly with each other
  - No need to implement scheduling yet
  - No concurrency management

## V0.1.0

- Annotation processor
  - Validates signatures, just if they are suspendable
  - Creates actor proxy, with just delegate capabilities
  - Creates base actor factory interfaces
- Change PoC to use generated code

## V0.1.1

- Implement one serialization strategy. The most versatile one.
- Make proxy prototype that works with serialized messages
  - This is not mailboxes
  - Introduce a proxy exclusive endpoint where all inbound communication flows

## V0.1.2

- Implements serialization validations on the code generator
- Change code generation to provide proxies with serialized message communication

## V0.1.3

- Implements the remaining two serialization strategies
- Change the code generator to use the other serialization strategies

## V0.2.0

- Implement mailboxes
- Change communication strategy to use the mailbox instead of direct messages
  - Only consume one message at the time (concurrency management)
  - Adapt the communication endpoint to use the mailbox
  - Implement a coroutine dispatcher for mailbox message scheduling
- Make a prototype with mailbox capabilities

## V0.2.1

- Change code generator to include mailboxes
- At this point a minimalistic PoC should be working

## TBD

- Error handling
- Actor address and identity as first class citizen
- Factory extensions
- Proxy life cycle management
