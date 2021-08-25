#!/bin/bash

export OMS_ROOT=`pwd`

#cd $OMS_ROOT

TARGET=$1
SCOPE=$2

echo "-- Start --"

function do_clean {
    echo "-- Clean --"
    cd $OMS_ROOT/services
    mvn clean
    if [ $? != 0 ]; then
	echo "Build failed - Clean services"
	exit -1;
    fi
    
    cd $OMS_ROOT/tests/jmeter
    rm -f *.csv
    if [ $? != 0 ]; then
	echo "Build failed - Clean jmeter test data"
	exit -1;
    fi

    cd $OMS_ROOT/analytics/hadoop/mapred
    mvn clean
    if [ $? != 0 ]; then
	echo "Build failed - Clean mapred"
	exit -1;
    fi

    if [ "$SCOPE" == "all" ]; then
	cd $OMS_ROOT/staging
	rm -rf services web analytics registry tests
	if [ $? != 0 ]; then
	    echo "Build failed - Clean staging"
	    exit -1;
	fi
	
	cd $OMS_ROOT/docker/bin
	./clean-files.sh
	if [ $? != 0 ]; then
	    echo "Build failed - Clean docker dirs"
	    exit -1;
	fi
	./clean-images.sh
	if [ $? != 0 ]; then
	    echo "Build failed - Clean dangling docker images"
	    exit -1;
	fi
    fi
	 
    echo "-- Done --"
}


function do_build {
    echo "-- Build --"
    cd $OMS_ROOT/services
    mvn package
    if [ $? != 0 ]; then
	echo "Build failed - Build Services failed"
	exit -1;
    fi

    cd $OMS_ROOT/tests/jmeter
    ./create-data.sh
    if [ $? != 0 ]; then
	echo "Build failed - Create jmeter test data failed"
	exit -1;
    fi

    # cd $OMS_ROOT/analytics/hadoop/mapred
    # mvn clean package -Dmaven.test.skip=true
    # if [ $? != 0 ]; then
    # 	echo "Build failed - Build Analytics failed"
    # 	exit -1;
    # fi
    
    cd $OMS_ROOT/web
    ./create-tgz.sh
    if [ $? != 0 ]; then
	echo "Build failed - Create Python UI Zip failed"
	exit -1;
    fi
    echo "-- Done --"
}

function do_stage {
    echo "-- Pull artifacts to Staging --"
    cd $OMS_ROOT/staging/bin
    ./pull-artifacts.sh
    if [ $? != 0 ]; then
	echo "Build failed -- Pull artifacts failed"
	exit -1;
    fi
    echo "-- Done --"
}

function do_images {
    echo "-- Pull artifacts from Staging to Docker images dir --"
    cd $OMS_ROOT/docker/bin
    ./pull-artifacts.sh
    if [ $? != 0 ]; then
	echo "Build failed -- Docker Pull artifacts failed"
	exit -1;
    fi
    echo "-- Done --"

    echo "-- Build Docker Images --"
    cd $OMS_ROOT/docker
    docker-compose build
    if [ $? != 0 ]; then
	echo "Build failed - Build images failed"
	exit -1;
    fi

    cd $OMS_ROOT/docker/jmeter
    docker-compose build
    if [ $? != 0 ]; then
	echo "Build failed - Build jmeter image failed"
	exit -1;
    fi
    echo "-- Done --"
}

function do_run {
    cd $OMS_ROOT/bin
    ./run.sh $1
}

if [ "$TARGET" == "clean" ]; then
    do_clean
elif [ "$TARGET" == "build" ]; then
    do_build
elif [ "$TARGET" == "stage" ]; then
    do_stage
elif [ "$TARGET" == "images" ]; then
    do_images
elif [ "$TARGET" == "run" ]; then
    do_run run
elif [ "$TARGET" == "start" ] ||
	 [ "$TARGET" == "test" ] ||
	 [ "$TARGET" == "stop" ]; then
    do_run $TARGET
else
    do_clean
    do_build
    do_stage
    do_images
fi
    
echo "-- Done --"
