#!/bin/bash

PROJECT=ntw-sample-sys
REGION=asia-southeast1


if [ -z "$1" ]; then
    NUM_INSTANCES=3
else
    NUM_INSTANCES=$1
fi

declare -a zone_arr=("asia-southeast1-a" "asia-southeast1-b" "asia-southeast1-c")

declare -a svc_arr=("web" "gateway-svc" "auth-svc" "admin-svc" "product-svc" "inventory-svc" "order-svc" "user-profile-svc")

for SERVICE in "${svc_arr[@]}"
do
    echo "-- Starting managed service -> ${SERVICE} --"

    gcloud compute instance-groups managed resize ${SERVICE} --size 0 --project ${PROJECT} --region ${REGION}

done


declare -a svc_arr=("web" "gateway-svc" "auth-svc" "admin-svc" "product-svc" "inventory-svc" "order-svc" "user-profile-svc")

for SERVICE in "${svc_arr[@]}"
do
    echo "-- Starting managed service -> ${SERVICE} --"

    gcloud compute instance-groups managed resize ${SERVICE} --size ${NUM_INSTANCES} --project ${PROJECT} --region ${REGION}

done
