#!/bin/bash

PROJECT=ntw-sample-sys
REGION=asia-southeast1
NUM_INSTANCES=3

declare -a zone_arr=("${REGION}-a" "${REGION}-b" "${REGION}-c")

for i in `seq 1 ${NUM_INSTANCES}`
do
    echo "-- Creating cassandra-${i} in ${zone_arr[i-1]} --"

    gcloud beta compute --project=${PROJECT} instances create cassandra-${i} --zone=${zone_arr[i-1]} --machine-type=g1-small --subnet=default --no-address --metadata=startup-script=export\ HOST_NAME=\$\(curl\ -s\ http://metadata.google.internal/computeMetadata/v1/instance/name\ -H\ Metadata-Flavor:Google\)$'\n'cd\ /home/anurag_yadav/minisys/demos/reliability$'\n'dc\ up\ -d\ \$HOST_NAME --no-restart-on-failure --maintenance-policy=TERMINATE --preemptible --service-account=271073603011-compute@developer.gserviceaccount.com --scopes=https://www.googleapis.com/auth/devstorage.read_only,https://www.googleapis.com/auth/logging.write,https://www.googleapis.com/auth/monitoring.write,https://www.googleapis.com/auth/servicecontrol,https://www.googleapis.com/auth/service.management.readonly,https://www.googleapis.com/auth/trace.append --image=minisys-build-1 --image-project=${PROJECT} --boot-disk-size=20GB --boot-disk-type=pd-standard --boot-disk-device-name=cassandra-${i} --no-shielded-secure-boot --shielded-vtpm --shielded-integrity-monitoring --reservation-affinity=any

done

for i in `seq 1 ${NUM_INSTANCES}`
do
    echo "-- Creating eureka-${i} in ${zone_arr[i-1]} --"

    gcloud beta compute --project=${PROJECT} instances create eureka-${i} --zone=${zone_arr[i-1]} --machine-type=g1-small --subnet=default --network-tier=PREMIUM --metadata=startup-script=export\ HOST_NAME=\$\(curl\ -s\ http://metadata.google.internal/computeMetadata/v1/instance/name\ -H\ Metadata-Flavor:Google\)$'\n'cd\ /home/anurag_yadav/minisys/demos/reliability$'\n'dc\ up\ -d\ \$HOST_NAME --no-restart-on-failure --maintenance-policy=TERMINATE --preemptible --service-account=271073603011-compute@developer.gserviceaccount.com --scopes=https://www.googleapis.com/auth/devstorage.read_only,https://www.googleapis.com/auth/logging.write,https://www.googleapis.com/auth/monitoring.write,https://www.googleapis.com/auth/servicecontrol,https://www.googleapis.com/auth/service.management.readonly,https://www.googleapis.com/auth/trace.append --image=minisys-build-1 --image-project=${PROJECT} --boot-disk-size=20GB --boot-disk-type=pd-standard --boot-disk-device-name=eureka-${i} --no-shielded-secure-boot --shielded-vtpm --shielded-integrity-monitoring --reservation-affinity=any

done
