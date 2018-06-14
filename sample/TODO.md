# ToDos
- epochal time model:
    - identity: db connection
    - value: db at point in time
- maintain commit log (i.e. index by transaction?)
- storage as simple storage
    - to store b+tree nodes
- transactor for processing transactions
    - notify peers of new transactions
- peers should be able to merge
- keep only necessary data in memory (only the stuff you asked about)
- hierarchical retrieval of data:
    1. local cache
    2. memcache
    3. storage
- storage can be read by peers directly
- compact current db value (remove overridden ones)

# Motivation
- don't forget history
- being able to work on data without worrying about change
- convey information to other parties and they will see the same
- should be like the real world works:
    - facts happen
    - no coordination required to observe (read)
    - facts don't get lost/forgotten when new ones happen

