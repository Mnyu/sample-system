#!/bin/bash

NUM_INSTANCES=2

WAIT_TIME_FOR_DB=20
WAIT_TIME_FOR_SVC=20

declare -a zone_arr=("asia-southeast1-b" "asia-southeast1-b" "asia-southeast1-b")

declare -a db_arr=("cassandra-1" "cassandra-2" "eureka-1" "postgres-1")
for DB in "${db_arr[@]}"
do
    echo "-- Creating ${DB} in asia-southeast1-b --"

    gcloud beta compute --project=ntw-sample-sys instances create ${DB} --zone=asia-southeast1-b --machine-type=g1-small --subnet=default --no-address --metadata=startup-script=export\ HOST_NAME=\$\(curl\ -s\ http://metadata.google.internal/computeMetadata/v1/instance/name\ -H\ Metadata-Flavor:Google\)$'\n'cd\ /home/anurag_yadav/minisys/demos/scalability$'\n'dc\ up\ -d\ \$HOST_NAME --no-restart-on-failure --maintenance-policy=TERMINATE --preemptible --service-account=271073603011-compute@developer.gserviceaccount.com --scopes=https://www.googleapis.com/auth/devstorage.read_only,https://www.googleapis.com/auth/logging.write,https://www.googleapis.com/auth/monitoring.write,https://www.googleapis.com/auth/servicecontrol,https://www.googleapis.com/auth/service.management.readonly,https://www.googleapis.com/auth/trace.append --image=minisys-build-1 --image-project=ntw-sample-sys --boot-disk-size=20GB --boot-disk-type=pd-standard --boot-disk-device-name=${DB} --no-shielded-secure-boot --shielded-vtpm --shielded-integrity-monitoring --reservation-affinity=any

done

if [ -n "${WAIT_TIME_FOR_DB}" ]; then
    count=0
    while [ $count -lt ${WAIT_TIME_FOR_DB} ]
    do
	sleep 1
	echo "=>"
	count=$(( $count + 1 ))
    done
fi

declare -a svc_arr=("web" "gateway-svc" "auth-svc" "admin-svc" "product-svc" "inventory-svc" "order-svc" "user-profile-svc")

for SERVICE in "${svc_arr[@]}"
do
    for i in `seq 1 ${NUM_INSTANCES}`
    do
	echo "-- Creating ${SERVICE}-${i} in ${zone_arr[i-1]} --"
	gcloud beta compute --project=ntw-sample-sys instances create ${SERVICE}-${i} --zone=${zone_arr[i-1]} --machine-type=g1-small --subnet=default --no-address --metadata=startup-script=export\ HOST_NAME=\$\(curl\ -s\ http://metadata.google.internal/computeMetadata/v1/instance/name\ -H\ Metadata-Flavor:Google\)$'\n'cd\ /home/anurag_yadav/minisys/demos/scalability$'\n'dc\ up\ -d\ \$HOST_NAME --no-restart-on-failure --maintenance-policy=TERMINATE --preemptible --service-account=271073603011-compute@developer.gserviceaccount.com --scopes=https://www.googleapis.com/auth/devstorage.read_only,https://www.googleapis.com/auth/logging.write,https://www.googleapis.com/auth/monitoring.write,https://www.googleapis.com/auth/servicecontrol,https://www.googleapis.com/auth/service.management.readonly,https://www.googleapis.com/auth/trace.append --image=minisys-build-1 --image-project=ntw-sample-sys --boot-disk-size=20GB --boot-disk-type=pd-standard --boot-disk-device-name=${SERVICE}-${i} --no-shielded-secure-boot --shielded-vtpm --shielded-integrity-monitoring --reservation-affinity=any
	
    done
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

declare -a lb_arr=("lb-web-1" "lb-rest-1")
for LB in "${lb_arr[@]}"
do
    echo "-- Creating ${DB} in asia-southeast1-b --"

    gcloud beta compute --project=ntw-sample-sys instances create ${LB} --zone=asia-southeast1-b --machine-type=g1-small --subnet=default --network-tier=PREMIUM --metadata=startup-script=export\ HOST_NAME=\$\(curl\ -s\ http://metadata.google.internal/computeMetadata/v1/instance/name\ -H\ Metadata-Flavor:Google\)$'\n'cd\ /home/anurag_yadav/minisys/demos/scalability$'\n'dc\ up\ -d\ \$HOST_NAME --no-restart-on-failure --maintenance-policy=TERMINATE --preemptible --service-account=271073603011-compute@developer.gserviceaccount.com --scopes=https://www.googleapis.com/auth/devstorage.read_only,https://www.googleapis.com/auth/logging.write,https://www.googleapis.com/auth/monitoring.write,https://www.googleapis.com/auth/servicecontrol,https://www.googleapis.com/auth/service.management.readonly,https://www.googleapis.com/auth/trace.append --image=minisys-build-1 --image-project=ntw-sample-sys --boot-disk-size=20GB --boot-disk-type=pd-standard --boot-disk-device-name=${LB} --no-shielded-secure-boot --shielded-vtpm --shielded-integrity-monitoring --reservation-affinity=any

done
