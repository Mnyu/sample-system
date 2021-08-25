#!/bin/bash

OMS_ROOT=../..
OMS_STAGING=$OMS_ROOT/staging
OMS_DEPLOY=$OMS_ROOT/docker

echo Copy PyUI to web
cp $OMS_STAGING/web/PyUI.tar.gz $OMS_DEPLOY/web/image

echo Copy PyUI to lb-web
cp $OMS_STAGING/web/PyUI.tar.gz $OMS_DEPLOY/lb-web/image

echo Copy Services war files
test -d $OMS_DEPLOY/rest/image/war || mkdir -p $OMS_DEPLOY/rest/image/war && \
	cp $OMS_STAGING/services/*.war $OMS_DEPLOY/rest/image/war

echo Copy Eureka Server war file
test -d $OMS_DEPLOY/eureka/image || mkdir -p $OMS_DEPLOY/eureka/image && \
	cp $OMS_STAGING/registry/DiscoverySvc.jar $OMS_DEPLOY/eureka/image

echo Copy Jmeter test files
test -d $OMS_DEPLOY/jmeter/image/tests || mkdir -p $OMS_DEPLOY/jmeter/image/tests && \
	cp $OMS_STAGING/tests/* $OMS_DEPLOY/jmeter/image/tests
test -d $OMS_DEPLOY/ubuntux/image/tests || mkdir -p $OMS_DEPLOY/ubuntux/image/tests && \
	cp $OMS_STAGING/tests/* $OMS_DEPLOY/ubuntux/image/tests

# echo Copy PySpark files
# test -d $OMS_DEPLOY/hadoop/image/pyspark || mkdir $OMS_DEPLOY/hadoop/image/pyspark && \
# 	cp $OMS_STAGING/analytics/pyspark/*.py $OMS_DEPLOY/hadoop/image/pyspark

# echo Copy MapRed jar files
# test -d $OMS_DEPLOY/hadoop/image/mapred || mkdir $OMS_DEPLOY/hadoop/image/mapred && \
# 	cp $OMS_STAGING/analytics/mapred/MapRed.jar $OMS_DEPLOY/hadoop/image/mapred && \
# 	cp -r $OMS_STAGING/analytics/mapred/lib $OMS_DEPLOY/hadoop/image/mapred

echo Done!!
