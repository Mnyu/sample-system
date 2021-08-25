#!/bin/bash

if [ -z "$1" ]; then
    echo "Usage push-images.sh <REGISTRY_PATH>"
    exit -1
fi

REGISTRY_PATH=$1

function tag_and_push {

    echo "Tag image ntw/$1"
    docker tag ntw/$1 ${REGISTRY_PATH}/$1:latest
    if [ $? != 0 ]; then
        echo "Tag image failed - $1"
        exit -1;
    fi

    echo "Push image $1"
    docker push ${REGISTRY_PATH}/$1:latest
    if [ $? != 0 ]; then
        echo "Push image failed - $1"
        exit -1;
    fi

}

declare -a arr=("cassandra" "postgres" "rest" "web" "lb-rest" "lb-web" "jmeter" "eureka")

for i in "${arr[@]}"
do
    tag_and_push $i
done

echo "-- Done --"
