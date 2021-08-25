#!/bin/bash

OMS_ROOT=`pwd`/..
cd $OMS_ROOT

TARGET=$1

echo "-- Start --"

function do_start {
    echo "-- Run Containers --"
    cd $OMS_ROOT/docker
    docker-compose up -d
    if [ $? != 0 ]; then
	echo "Build failed - Run containers failed"
	exit -1;
    fi
    echo "-- Done --"
}

function do_test {
    echo "-- Run tests --"
    cd $OMS_ROOT/docker/jmeter
    docker-compose build
    docker-compose up
    if [ $? != 0 ]; then
	echo "Tests failed - Unable to start Jmeter container"
	exit -1;
    fi
    echo "-- Done --"
}

function do_stop {
    echo "-- Stop containers --"
    cd $OMS_ROOT/docker
    docker-compose stop
    if [ $? != 0 ]; then
	echo "Build failed - Cannot stop containers"
	exit -1;
    fi
    echo "-- Done --"
}

if [ "$TARGET" == "start" ]; then
    do_start
elif [ "$TARGET" == "test" ]; then
    do_test
elif [ "$TARGET" == "stop" ]; then
    do_stop
else
    do_start
    i="0"
    RESPONSE=$(curl -s -I -X GET http://localhost:9000/GatewaySvc/status | head -n 1)
    echo "$i - $RESPONSE"
    while [ "$(echo $RESPONSE | cut -d$' ' -f2)" != "200" ] && [ $i -lt 120 ];
    do sleep 5;
       RESPONSE=$(curl -s -I -X GET http://localhost:9000/GatewaySvc/status | head -n 1)
       i=$[$i+5]
       echo "$i - $RESPONSE"
    done;
    sleep 5
    do_test
    do_stop
fi
    
echo "-- Done --"
