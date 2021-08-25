#!/bin/bash

# CMD options are: create, apply, delete

if [[ -z "$1" ]]; then
    CMD="apply"
else
    CMD=$1
fi

declare -a arr=("config-map" "secrets-map" "cassandra" "postgres" "web" "admin" "gateway" "admin" "auth" "userprofile" "product" "order" "inventory")

for COMPONENT in "${arr[@]}"
do
    echo "-- $CMD $COMPONENT --"
    kubectl $CMD -f ${COMPONENT}.yaml
done
