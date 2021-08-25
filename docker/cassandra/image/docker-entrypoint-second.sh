#!/bin/bash

if [ -z "${REPLICATION_FACTOR}" ]; then
    REPLICATION_FACTOR=1
fi

function create_schema {
    until cat /create-schema.cql | cqlsh; do
	echo "cqlsh: Cassandra is unavailable - retry later"
	sleep 2
    done
}

sed -i -e 's/#REPLICATION_FACTOR#/'${REPLICATION_FACTOR}'/g' /create-schema.cql

if [[ $(hostname -s) = ${SCHEMA_SEED_INSTANCE} ]]; then
    create_schema &
fi

exec /docker-entrypoint.sh "$@"
