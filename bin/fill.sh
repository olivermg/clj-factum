#!/bin/bash

INSERT="insert into es_events (uuid, entity, action, body, inserted_at) values"
SEP=" "

echo -n "${INSERT}"
for I in 1 2 3; do
    echo -n -e "${SEP}\n(1,'e1','a1','foo','bar')"
    SEP=","
done
echo ";"
