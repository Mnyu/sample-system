#!/bin/bash

if [ -z "$1" ]; then
    echo "Usage gcp-pull-images.sh <PROJECT_ID>"
    exit -1
fi

PROJECT_ID=$1
ZONE=$2

function pull_and_tag {

    echo "Pull image $1"
    docker pull asia.gcr.io/${PROJECT_ID}/$1:latest
    if [ $? != 0 ]; then
        echo "Pull image failed - $1"
        exit -1;
    fi

    echo "Tag image ntw/$1"
    docker tag asia.gcr.io/${PROJECT_ID}/$1:latest ntw/$1
    if [ $? != 0 ]; then
        echo "Tag image failed - $1"
        exit -1;
    fi

}

declare -a arr=("cassandra" "postgres" "rest" "web" "eureka")

for i in "${arr[@]}"
do
    pull_and_tag $i
done

echo "-- Done --"

