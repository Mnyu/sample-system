#!/bin/bash

NUM_INSTANCES=2

WAIT_TIME_FOR_DB=20
WAIT_TIME_FOR_SVC=20

PROJECT=ntw-sample-sys
ZONE=asia-southeast1-b

declare -a svc_arr=("eureka-1" "postgres-1" "web-1" "gateway-svc-1" "auth-svc-1" "admin-svc-1" "product-svc-1" "inventory-svc-1" "order-svc-1" "user-profile-svc-1")
for SVC in "${svc_arr[@]}"
do
    echo "-- Creating ${SVC} in ${ZONE} --"

    #gcloud beta compute --project=ntw-sample-sys instances create ${SVC} --zone=${ZONE} --machine-type=g1-small --subnet=subnet-pvt-1 --no-address --metadata=startup-script=export\ HOST_NAME=\$\(curl\ -s\ http://metadata.google.internal/computeMetadata/v1/instance/name\ -H\ Metadata-Flavor:Google\)$'\n'cd\ /home/anurag_yadav/minisys/demos/security$'\n'dc\ up\ -d\ \$HOST_NAME --no-restart-on-failure --maintenance-policy=TERMINATE --preemptible --service-account=271073603011-compute@developer.gserviceaccount.com --scopes=https://www.googleapis.com/auth/devstorage.read_only,https://www.googleapis.com/auth/logging.write,https://www.googleapis.com/auth/monitoring.write,https://www.googleapis.com/auth/servicecontrol,https://www.googleapis.com/auth/service.management.readonly,https://www.googleapis.com/auth/trace.append --image=minisys-build-1 --image-project=ntw-sample-sys --boot-disk-size=20GB --boot-disk-type=pd-standard --boot-disk-device-name=${SVC} --no-shielded-secure-boot --shielded-vtpm --shielded-integrity-monitoring --reservation-affinity=any

done


if [ -n "${WAIT_TIME_FOR_SVC}" ]; then
    count=0
    while [ $count -lt ${WAIT_TIME_FOR_SVC} ]
    do
	sleep 1
	echo "=>"
	count=$(( $count + 1 ))
    done
fi

echo "-- Creating lb-rest-1 in asia-southeast1-b --"

    gcloud beta compute --project=ntw-sample-sys instances create lb-rest-1 --zone=asia-southeast1-b --machine-type=g1-small --subnet=subnet-pvt-1 --no-address --metadata=startup-script=export\ HOST_NAME=\$\(curl\ -s\ http://metadata.google.internal/computeMetadata/v1/instance/name\ -H\ Metadata-Flavor:Google\)$'\n'cd\ /home/anurag_yadav/minisys/demos/security$'\n'dc\ up\ -d\ \$HOST_NAME --no-restart-on-failure --maintenance-policy=TERMINATE --preemptible --service-account=271073603011-compute@developer.gserviceaccount.com --scopes=https://www.googleapis.com/auth/devstorage.read_only,https://www.googleapis.com/auth/logging.write,https://www.googleapis.com/auth/monitoring.write,https://www.googleapis.com/auth/servicecontrol,https://www.googleapis.com/auth/service.management.readonly,https://www.googleapis.com/auth/trace.append --image=minisys-build-1 --image-project=ntw-sample-sys --boot-disk-size=20GB --boot-disk-type=pd-standard --boot-disk-device-name=${LB} --no-shielded-secure-boot --shielded-vtpm --shielded-integrity-monitoring --reservation-affinity=any

echo "-- Creating lb-web-1 in asia-southeast1-b --"

    gcloud beta compute --project=ntw-sample-sys instances create lb-web-1 --zone=asia-southeast1-b --machine-type=g1-small --subnet=subnet-pub-1 --network-tier=PREMIUM --metadata=startup-script=export\ HOST_NAME=\$\(curl\ -s\ http://metadata.google.internal/computeMetadata/v1/instance/name\ -H\ Metadata-Flavor:Google\)$'\n'cd\ /home/anurag_yadav/minisys/demos/security$'\n'dc\ up\ -d\ \$HOST_NAME --no-restart-on-failure --maintenance-policy=TERMINATE --preemptible --service-account=271073603011-compute@developer.gserviceaccount.com --scopes=https://www.googleapis.com/auth/devstorage.read_only,https://www.googleapis.com/auth/logging.write,https://www.googleapis.com/auth/monitoring.write,https://www.googleapis.com/auth/servicecontrol,https://www.googleapis.com/auth/service.management.readonly,https://www.googleapis.com/auth/trace.append --image=minisys-build-1 --image-project=ntw-sample-sys --boot-disk-size=20GB --boot-disk-type=pd-standard --boot-disk-device-name=${LB} --no-shielded-secure-boot --shielded-vtpm --shielded-integrity-monitoring --reservation-affinity=any


