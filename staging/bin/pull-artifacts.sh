#!/bin/bash

OMS_ROOT=../..
OMS_STAGING=$OMS_ROOT/staging

echo Create directories
test -d $OMS_STAGING/services || mkdir -p $OMS_STAGING/services && \
test -d $OMS_STAGING/registry || mkdir -p $OMS_STAGING/registry && \
test -d $OMS_STAGING/web || mkdir -p $OMS_STAGING/web && \
test -d $OMS_STAGING/tests || mkdir -p $OMS_STAGING/tests && \
test -d $OMS_STAGING/analytics/pyspark || mkdir -p $OMS_STAGING/analytics/pyspark && \
test -d $OMS_STAGING/analytics/mapred || mkdir -p $OMS_STAGING/analytics/mapred

echo Copy PyUI .tar.gz file
cp -r $OMS_ROOT/web/PyUI.tar.gz $OMS_STAGING/web

echo Copy Services war files
cp $OMS_ROOT/services/target/*.war $OMS_STAGING/services

echo Copy eureka.jar file
cp -r $OMS_ROOT/services/target/DiscoverySvc.jar $OMS_STAGING/registry

echo Copy Jmeter tests files
cp -r $OMS_ROOT/tests/jmeter/* $OMS_STAGING/tests

#echo Copy Analytics PySpark files
#cp $OMS_ROOT/analytics/hadoop/spark/*.py $OMS_STAGING/analytics/pyspark

#echo Copy Analytics MapRed jar files
#cp $OMS_ROOT/analytics/hadoop/mapred/target/MapRed-1.0-SNAPSHOT.jar $OMS_STAGING/analytics/mapred/MapRed.jar
#cp -r $OMS_ROOT/analytics/hadoop/mapred/target/lib $OMS_STAGING/analytics/mapred/

echo Done!!
