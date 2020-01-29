# Hollywood

A minimalistic type safe actor based framework built with and for kotlin.

This project leverages several technologies to provide an easier way to handle concurrent programing. It does not intend to be fully compliant with all the requirements of the actor model, just borrows some of it's main ideas.

It is also not a requirement to design against a multi clustered implementation. This is a next level problem that requires some complicated guarantees like ensuring an actor is not created on multiple clusters. The current design should work on a single machine in order to facilitate some of the work required.

**Disclaimer: Just a PoC at this stage**

## Index

- [Type safe actors](./doc/Actors.md)
- [Communication](./doc/Communication.md)
- [Code Generation](./doc/CodeGeneration.md)
- [State Persistence](./doc/StatePersistence.md)