export HOST_NAME=$(curl -s http://metadata.google.internal/computeMetadata/v1/instance/name -H Metadata-Flavor:Google)
cd /home/anurag_yadav/minisys/demos/reliability
HOST_NAME_PREFIX=$(sed 's/-svc.*/-svc/g' <<< ${HOST_NAME})
dc up -d $HOST_NAME_PREFIX
