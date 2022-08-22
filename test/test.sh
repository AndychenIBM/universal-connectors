#!/bin/bash

echo "==========================test images status (docker images):=========================="
docker images
echo "==========================test container status (docker ps -a):=========================="
docker ps -a
docker run -d --name="Klaus" --network="host" -e UC_LOG_LEVEL="DEBUG" -it universal-connector
installedPluginsNum=$(docker exec -it Klaus bash -c "logstash-plugin list --verbose | grep guardium | wc -l")
echo "num of installed plugins: ${installedPluginsNum}"

if [ "$installedPluginsNum" -lt 5 ]; then
  echo "Missing Guardium plugins"
  exit 1
fi

echo "Searching for output_to_guardium plugin..."
docker exec -it Klaus bash -c "logstash-plugin list --verbose | grep output_to_guardium"
if [ $? -eq 0 ]; then
  echo "Found output_to_guardium"
else
  echo "output_to_guardium plugin is missing from Universal Connector image"
  exit 1
fi