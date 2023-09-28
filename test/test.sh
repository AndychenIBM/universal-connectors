#!/bin/bash

UC_IMAGE_NAME="universal-connector"
UC_CONTAINER_NAME="Klaus"
LOGSTASH_PLUGIN_LIST_FILE=logstash-plugin-list.txt
MINIMUM_AMOUNT_OF_PLUGINS=20

function testMandatoryPluginExistence(){
  if ! grep $1 ${LOGSTASH_PLUGIN_LIST_FILE}
  then
    echo "Missing $1. Exiting..."
    exit 1
fi
}

# Check image status
echo "========================== check images status (docker images | grep ${UC_IMAGE_NAME}): =========================="
docker images | grep ${UC_IMAGE_NAME}

# Check container status after creation
docker rm -f ${UC_CONTAINER_NAME} > /dev/null 2>&1
docker run -d --name=${UC_CONTAINER_NAME} --network="host" -e UC_LOG_LEVEL="DEBUG" -it universal-connector
echo "========================== check container status (docker ps -a | grep ${UC_CONTAINER_NAME}): =========================="
docker ps -a | grep ${UC_CONTAINER_NAME}

# Print logstash-plugin list
docker exec -it Klaus bash -c "logstash-plugin list --verbose &> ${LOGSTASH_PLUGIN_LIST_FILE}"
docker cp Klaus:/usr/share/logstash/${LOGSTASH_PLUGIN_LIST_FILE} .

# Test 1: check at least ${MINIMUM_AMOUNT_OF_PLUGINS} installed plugins with Guardium name
installedPluginsNum=$(grep guardium ${LOGSTASH_PLUGIN_LIST_FILE} | wc -l)
echo "num of installed plugins that contain the name guardium: ${installedPluginsNum}"
if [[ "$installedPluginsNum" -lt ${MINIMUM_AMOUNT_OF_PLUGINS} ]]; then
  echo "Missing Guardium plugins. Please check if UC default offline package was installed."
  exit 1
fi

# Test 2: verify mandatory plugins existence
testMandatoryPluginExistence "output-guardium"
testMandatoryPluginExistence "input-cloudwatch_logs"

# End test with a list of installed plugins
echo "Plugins installed:"
cat ${LOGSTASH_PLUGIN_LIST_FILE}

rm -rf ${LOGSTASH_PLUGIN_LIST_FILE}
