export HOST_NAME=$(curl -s http://metadata.google.internal/computeMetadata/v1/instance/name -H Metadata-Flavor:Google)
cd /home/anurag_yadav/minisys/demos/scalability
dc up -d $HOST_NAME
