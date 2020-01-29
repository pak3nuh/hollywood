# Hollywood research

Before the actual project started, some research has been conducted to set the scope and objectives. The research was not meant to be exhaustive, nor the project aims to be compliant with all the actor model restrictions. It just gathers enough information to compile a plan of action.

Main objectives are:
- Type safety everywhere
- Framework with little intrusion
  - Doesn't force a base actor class
  - Some restrictions may apply for type safety
- Easy to plugin a IoC container
- All code can be inspected and debugged by using code generation
- Provides the typical concurrency guarantees of the actor model

## Index

- [Type safe actors](./Actors.md)
- [Communication](./Communication.md)
- [Code Generation](./CodeGeneration.md)
- [State Persistence](./StatePersistence.md)
- [Expected Deliverables](./Deliverables.md)