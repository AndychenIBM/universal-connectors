#!/bin/bash

UC_IMAGE_NAME="universal-connector"
UC_CONTAINER_NAME="Klaus"
LOGSTASH_PLUGIN_LIST_FILE=logstash-plugin-list.txt
MINIMUM_AMOUNT_OF_PLUGINS=15

UC_PLUGIN_REPO_BRANCH=main
UC_OPENSOURCE_ROOT_DIR=universal-connectors-${UC_PLUGIN_REPO_BRANCH}

function testPluginExistence() {
  pluginName=${1##*/}
  pluginName=${pluginName%-*} # removed version

  installedVersion=$(grep "${pluginName}" ${LOGSTASH_PLUGIN_LIST_FILE} | cut -d ")" -f1 | cut -d "(" -f2 | xargs)

  parentDir="$(dirname "${UC_OPENSOURCE_ROOT_DIR}/$1")"
  expectedVersion=$(cat ${parentDir}/VERSION | xargs)

  if [ "$expectedVersion" = "$installedVersion" ]; then
    echo "Plugin $pluginName exists in UC image. Version installed: $expectedVersion"
  elif [ -z "$installedVersion" ]; then
    echo "Warning: Plugin ${pluginName} is not installed in UC image"
  else
    echo "Warning: installed version of plugin $pluginName is $installedVersion and the version stated in Github ${parentDir}/VERSION file is $expectedVersion"
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

# Test 2: verify output plugin existence
if grep "output_to_guardium" ${LOGSTASH_PLUGIN_LIST_FILE}
then
    outputInstalledVersion=$(grep "output_to_guardium" ${LOGSTASH_PLUGIN_LIST_FILE} | cut -d ")" -f1 | cut -d "(" -f2 | xargs)
    echo "Logstash output plugin to Guardium exists. Version: ${outputInstalledVersion}"
else
    echo "Missing Guardium output plugin for Universal Connector"
    exit 1
fi

# Test 3: check plugin existence and version in UC image
grep -v '^#' defaultOfflinePackagePlugins.txt | while read -r line; do testPluginExistence "${line}"; done




rm -rf ${LOGSTASH_PLUGIN_LIST_FILE}
