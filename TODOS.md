# Eventsourcing


## Terms

- DiskDB:
  Underlying Database (e.g. PostgreSQL).

- MemDB:
  Copy of DiskDB in memory of Application.


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
