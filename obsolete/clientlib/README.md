# clj-factum

Eventsourcing made simple for common databases (Postgres for now).


## State

This project is subject to change in every matter you can imagine. There
is no stable API or anything like that yet. I am working on it.


## Terms

- Fact:
  A single datum in a DB. Effectively a tuple consisting of
    - entity-id (e)
    - attribute (a)
    - value (v)
    - transaction (t)

- ExternalDB:
  Underlying Database (e.g. PostgreSQL) used to provide storage
  mechanisms for our data.

- Topic:
  A criteria that applies to certain facts in a DB.

- MemDB:
  Copy of ExternalDB in memory of Application, potentially containing only
  parts of ExternalDB for a specific topic.

- MemDBCache:
  Container for zero, one or more MemDBs.


## Layers

Should be separated into

- Reading (potentially multiple ways)
- Writing

Rough layout:

1. DB Layer that talks to ExternalDB, without knowing anything about
   facts etc. (e.g. `select-lazy`).

2. Layer that knows about facts and how to retrieve & store those
   (e.g. `add-fact`, `get-fact`).

3. Layer that knows how to handle facts in terms of core.logic.

4. Application specific layer that defines business logical entities?


## Ideas

- "Snapshots":
  Either really in the DB (delete obsolete facts up to a certain date) or
  provide "virtual" snapshots on App side.
  This can exist in two variants:
  - for MemDB, to save application memory
  - for ExternalDB, to save disk space

- MemDB (DB in memory):
  Keep just one instance of DB in App memory and operate on that.

- DB instances per timestamp:
  Implement DB references that refer to a certain point in time, so the user
  can query the DB at a point in time. It should be sufficient if this is just
  the desired timestamp. When querying, the query logic can operate on its
  MemDB with respect to the specified timestamp.

- Polling DB:
  The App should poll the DB for new facts every N seconds (as the DB does
  not offer any kind of push mechanism). The SQL query should only select
  facts that are newer than the ones the App already knows about.

- Updating own MemDB:
  When adding or retracting facts, the MemDB can be updated as well, so it
  does not need to wait for the next poll.

- Datalog language:
  For less verbose querying, we might develop a query language like Datalog
  that acts as a wrapper (maybe as macros) on core.logic syntax.

- MemDB topic separation:
  Maybe it makes sense to build a MemDB from the ExternalDB by only retrieving
  facts belonging to a certain topic (e.g. a specific customer). Via this way,
  overly expensive memory consumption is being avoided.
  Thus, the application would have multiple MemDBs, each for a certain topic.
  The app could maintain a "MemDBCache", keeping only the most recently used MemDBs
  in memory while forgetting those that haven't been used in a while.
  When a request comes in that demands for a MemDB that is not in the "MemDBCache",
  the app needs to retrieve that one from the ExternalDB.

- Resolving entities:
  We need some means by which we can retrieve an entire entity.

- Schemas:
  When defining schemas for entities, that would avoid lots of work, as the
  entity resolving logic would know when an entity has been found in it's
  entirety. Without schemas, it will always have to look through all facts.

- Lazy relations:
  It would be nice to be able to traverse related entities. For that, we need
  to maintain `lazy-seq`s within an entity that retrieve the related entities
  when traversed.
  We also need to take care of that we don't end up in endless recursions when
  trying to print (define `print-method`) or serialize (?) entities.


## TODOs

- Remove dependency on Postgres - make DB config/connection completely external
  and only rely on clojure.java.jdbc .


## Examples

### Flow of Data

``` text
(get-facts) [retrieves all stored facts from database]
    |
    v
(project-facts) [projects to relevant facts, i.e. ones that are obsolete because of newer facts are being removed]
    |
    v
(get-logic-db) ["converts" db facts to a set of facts suitable for core.logic]
    |
    v
e.g. (get-entity) [applies core.logic program to a set of facts in order to retrieve desired data]
```
