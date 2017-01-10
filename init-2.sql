/*
different approach (inspired by datomic):
 - only store events, don't bother about querying on db side
 - let client side do querying (e.g. via core.logic)
 - implement snapshots, so clients don't always have to fetch
   entire data (does that make sense? what other means can we
   think of that reduce need to transmit data between client & server?)
*/

set client_encoding = 'UTF8';

drop table if exists es_events;
drop type if exists action;

create type action as enum ( 'add', 'retract' );

create table es_events (
       id serial not null,
       action action not null,
       entity text not null,
       uuid uuid not null,
       body jsonb not null,
       inserted_at timestamp with time zone not null default current_timestamp,

       primary key (id)
);
create index on es_events (uuid);
create index on es_events (entity, inserted_at);
create index on es_events (inserted_at);
