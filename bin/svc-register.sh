#!/bin/bash

REGISTRY_HOST=localhost
REGISTRY_PORT=9090

if [ "$1" == "register" ]; then
    while true; do
	 curl -X POST -H "Content-Type: application/json" --show-error -D -  --data @registry_data.json http://${REGISTRY_HOST}:${REGISTRY_PORT}/eureka/v2/apps/RESTSERVICE
	if [ "$?" != "0" ]; then
	    echo "Failed to register -- Waiting for 10 sec";
	    sleep 10;
	else
	    break;
	fi
    done
else
    curl -H "Accept: application/json" -H "Accept-Encoding: gzip" http://${REGISTRY_HOST}:${REGISTRY_PORT}/eureka/v2/apps | gzip -d | jq
fi
