# Eventsourcing


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


## Ideas

- Snapshots:
  Either really in the DB (delete obsolete facts up to a certain date) or
  provide "virtual" snapshots on App side.

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
