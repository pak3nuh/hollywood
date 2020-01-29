**Actor identity != actor address**

When an actor receives a message it can do 3 things
1. send more messages
2. create more actors
3. decide what to do on the next received message

It is possible to create a "future" actor to get a value that will be computed later. These futures can be passed around like any other actor, in fact they are just plain addresses for objects that don't exist, that will be materialized with an actor instance later in the future.