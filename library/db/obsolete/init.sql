set client_encoding = 'UTF8';

drop materialized view if exists user_view;
drop function if exists view_entity(text);
drop function if exists view_entity(text, timestamp with time zone);
drop aggregate if exists jsonb_merge_agg(jsonb);
drop function if exists jsonb_merge(jsonb, jsonb);
drop table if exists es_events;
drop type if exists action;

create type action as enum ( 'set', 'delete' );

create table es_events (
       id serial not null,
       uuid uuid not null,
       entity text not null,
       action action not null,
       body jsonb not null,
       inserted_at timestamp with time zone not null default current_timestamp,

       primary key (id)
);
create index on es_events (uuid);
create index on es_events (entity, inserted_at);
create index on es_events (inserted_at);

create function jsonb_merge(a jsonb, b jsonb)
       returns jsonb
       as $$
       begin
                return a || b;
       end;
$$ language plpgsql;

create aggregate jsonb_merge_agg(jsonb) (
       sfunc = jsonb_merge,
       stype = jsonb,
       initcond = '{}'
);

create function view_entity(wanted_entity text, wanted_time timestamp with time zone)
       returns table (uuid uuid, body jsonb, modified timestamp with time zone)
       as $$
       with eo as (
            select * from es_events
                   where entity = wanted_entity and inserted_at <= wanted_time
                   order by inserted_at
       )
       select uuid, jsonb_merge_agg(body), max(inserted_at)
              from eo
              group by uuid
$$ language sql;

create function view_entity(wanted_entity text)
       returns table (uuid uuid, body jsonb, modified timestamp with time zone)
       as $$
       select * from view_entity(wanted_entity, now())
$$ language sql;

create materialized view user_view as
       select * from view_entity('user');


insert into es_events (uuid, entity, action, body, inserted_at)
       values ('11111111-1111-1111-1111-111111111111', 'user', 'set', '{"name": "foo1", "gender": "m"}', '2017-01-01');
insert into es_events (uuid, entity, action, body, inserted_at)
       values ('11111111-1111-1111-1111-111111111111', 'user', 'set', '{"name": "foo2"}', '2017-01-03');
insert into es_events (uuid, entity, action, body, inserted_at)
       values ('11111111-1111-1111-1111-111111111111', 'user', 'set', '{"name": "foo1"}', '2017-01-02');
insert into es_events (uuid, entity, action, body, inserted_at)
       values ('11111111-1111-1111-1111-111111111111', 'user', 'set', '{"street": "street1"}', '2017-01-04');
insert into es_events (uuid, entity, action, body, inserted_at)
       values ('11111111-1111-1111-1111-111111111111', 'user', 'set', '{"gender": "f"}', '2017-01-05');

insert into es_events (uuid, entity, action, body, inserted_at)
       values ('11111111-1111-1111-1111-111111111112', 'user', 'set', '{"name": "bar1"}', '2017-01-01');
insert into es_events (uuid, entity, action, body, inserted_at)
       values ('11111111-1111-1111-1111-111111111112', 'user', 'set', '{"name": "bar2"}', '2017-01-02');

refresh materialized view user_view;
