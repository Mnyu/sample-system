#!/bin/bash

if [ -z "$1" ]; then
    CMD="create"
else
    CMD=$1
fi

REVISION_ID=latest
if [ -z "${IMAGE_REGISTRY_PATH}" ]; then
    IMAGE_REGISTRY_PATH="asia.gcr.io\/$(gcloud config get-value project)"
fi
echo "Docker Image Registry Path: ${IMAGE_REGISTRY_PATH}"

cd ./config


sed 's/IMAGE_REGISTRY_PATH/'${IMAGE_REGISTRY_PATH}'/g' jmeter.yaml \
    | sed 's/REVISION_ID/'${REVISION_ID}'/g' \
    | kubectl $CMD -f -

if [ "$CMD" != "delete" ]; then
    echo "-- Waiting for jmeter container to start --"
    sleep 15
    kubectl logs -f $(kubectl get pods --no-headers -o custom-columns=:metadata.name -l job-name=jmeter)

    sed 's/IMAGE_REGISTRY_PATH/'${IMAGE_REGISTRY_PATH}'/g' jmeter.yaml \
	| sed 's/REVISION_ID/'${REVISION_ID}'/g' \
	| kubectl delete -f -

fi

