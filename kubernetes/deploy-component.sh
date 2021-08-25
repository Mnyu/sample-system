#!/bin/bash


if [ -z "$1" ]; then
    printf "Usage: \nArg1: Component \nArg2: Command -> apply or delete\n"
    exit -1
fi

if [ -z "$2" ]; then
    CMD="apply"
else
    CMD=$2
fi

REVISION_ID=latest

if [ -z "${IMAGE_REGISTRY_PATH}" ]; then
    IMAGE_REGISTRY_PATH="asia.gcr.io\/$(gcloud config get-value project)"
fi
echo "Docker Image Registry Path: ${IMAGE_REGISTRY_PATH}"

COMPONENT=$1

cd ./config

echo "-- $CMD $COMPONENT --"
sed 's/IMAGE_REGISTRY_PATH/'${IMAGE_REGISTRY_PATH}'/g' ${COMPONENT}.yaml \
    | sed 's/REVISION_ID/'${REVISION_ID}'/g' \
    | kubectl ${CMD} -f -
