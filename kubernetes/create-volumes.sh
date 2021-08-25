#!/bin/bash

if [[ -z "$1" ]]; then
    CMD="apply"
else
    CMD=$1
fi

NODES=$(kubectl get nodes -o name | sed 's#^node/##' | xargs)

cd ./config

COMPONENT=postgres
arr=($NODES)
echo "$CMD volume for $COMPONENT on node ${arr[0]}"
sed 's/COMPONENT/'${COMPONENT}'/g' persistent-volumes.yaml \
    | sed 's/NODEIX/'1'/g' \
    | sed 's/NODENAME/'${arr[0]}'/g' \
    | kubectl $CMD -f -

COMPONENT=cassandra
counter=0    
for NODE in $NODES
do
    echo "$CMD volume for $COMPONENT on node $NODE"
    sed 's/COMPONENT/'${COMPONENT}'/g' persistent-volumes.yaml \
	| sed 's/NODEIX/'$counter'/g' \
	| sed 's/NODENAME/'${NODE}'/g' \
	| kubectl $CMD -f -
    
    ((counter++))
    if [ $counter -gt 3 ]; then
	break;
    fi
done

if [ "$CMD" != "delete" ]; then
    echo "$CMD persistent volume claim for postgres"
    kubectl $CMD -f persistent-volume-claims.yaml
else
    echo "$CMD all persistent volume claims"
    kubectl delete pvc --all
    echo "$CMD all persistent volumes"
    kubectl delete pv --all
fi
