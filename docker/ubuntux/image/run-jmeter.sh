#!/bin/bash

REST_HOST=lbrest
REST_PORT=80

$JMETER_HOME/bin/jmeter -n -t /usr/jmeter-data/test-plan.jmx -Jusers=1 -Jcount=1 -DREST_HOST=${REST_HOST} -DREST_PORT=${REST_PORT}
