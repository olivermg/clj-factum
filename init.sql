set client_encoding = 'UTF8';

drop table if exists es_events;
create table es_events (
       id serial not null,
       uuid uuid not null,
       type text not null,
       body jsonb not null,
       inserted_at timestamp with time zone default current_timestamp,

       primary key (id)
);
create index on es_events (uuid);
create index on es_events (type);
create index on es_events (inserted_at desc);
