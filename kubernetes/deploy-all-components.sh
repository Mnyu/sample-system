#!/bin/bash


if [ -z "$1" ]; then
    CMD="apply"
else
    CMD=$1
fi

REVISION_ID=latest
if [ -z "${IMAGE_REGISTRY_PATH}" ]; then
    IMAGE_REGISTRY_PATH="asia.gcr.io\/$(gcloud config get-value project)"
fi
echo "Docker Image Registry Path: ${IMAGE_REGISTRY_PATH}"

declare -a arr=("config-map" "secrets-map" \
			     "cassandra" "postgres" \
			     "gateway-svc" "admin-svc" \
			     "auth-svc" "user-profile-svc" "product-svc" "order-svc" "inventory-svc" \
			     "web-app")

cd ./config

for COMPONENT in "${arr[@]}"
do

    echo "-- $CMD $COMPONENT --"
    sed 's/IMAGE_REGISTRY_PATH/'${IMAGE_REGISTRY_PATH}'/g' ${COMPONENT}.yaml \
	| sed 's/REVISION_ID/'${REVISION_ID}'/g' \
	| kubectl $CMD -f -

done
