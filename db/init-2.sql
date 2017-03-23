/*
different approach (inspired by datomic):
 - only store events, don't bother about querying on db side
 - let client side do querying (e.g. via core.logic)
 - implement snapshots, so clients don't always have to fetch
   entire data (does that make sense? what other means can we
   think of that reduce need to transmit data between client & server?)
   - lazy db select (selecting chunks of data)
*/

set client_encoding = 'UTF8';


/*drop function if exists es_events_add(bigint, varchar(255), varchar(20), text);*/
drop sequence if exists es_events_txid;
drop table if exists es_events;


create table es_events (
       id bigserial not null,
       tx bigint not null,
       eid bigint not null,
       attribute varchar(255) not null,
       action varchar(20) not null default ':add',
       value text not null,

       primary key (id)
);
create index on es_events (tx desc);

create sequence es_events_txid;

/*
create function es_events_add(eid bigint, attribute varchar(255), action varchar(20), value text)
returns bigint
as $$
declare
        txid bigint;
        result bigint;
begin
        select nextval('es_events_txid') into txid;
        insert into es_events (tx, eid, attribute, action, value) values
               (txid, eid, attribute, action, value)
               returning id into result;
        return result;
end;
$$ language plpgsql;
*/


/*
select es_events_add(1, ':user/name',     ':add', '"foo2"');
select es_events_add(1, ':user/gender',   ':add', ':m');
select es_events_add(1, ':user/gender',   ':add', ':f');
select es_events_add(1, ':user/name',     ':add', '"foo1"');
select es_events_add(1, ':user/birthday', ':add', '"1999-09-09"');

select es_events_add(2, ':user/name',     ':add', '"bar2"');
select es_events_add(2, ':user/gender',   ':add', ':f');
select es_events_add(2, ':user/name',     ':add', '"bar1"');
*/

insert into es_events (tx, eid, attribute, action, value) values
       (1, 1, ':user/name',     ':add', '"foo2"'),
       (1, 1, ':user/gender',   ':add', ':m'),
       (3, 1, ':user/gender',   ':add', ':f'),
       (3, 1, ':user/name',     ':add', '"foo1"'),
       (3, 1, ':user/birthday', ':add', '"1999-09-09"'),

       (2, 2, ':user/name',     ':add', '"bar2"'),
       (2, 2, ':user/gender',   ':add', ':f'),
       (4, 2, ':user/name',     ':add', '"bar1"');
