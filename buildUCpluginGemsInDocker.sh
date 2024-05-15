#!/bin/bash

UC_PLUGIN_REPO_BRANCH=release-v1.5.5
UC_OPENSOURCE_ROOT_DIR=universal-connectors-${UC_PLUGIN_REPO_BRANCH}

chmod -R 777 *
cd test
docker build -t guc_dit:latest .
cd ..

# Pull UC plugins from open-source repo- used for uc commons only
rm -rf ${UC_OPENSOURCE_ROOT_DIR} ${UC_PLUGIN_REPO_BRANCH}.zip
wget https://github.com/IBM/universal-connectors/archive/refs/heads/${UC_PLUGIN_REPO_BRANCH}.zip && unzip -q ${UC_PLUGIN_REPO_BRANCH}.zip
cp -R ./logstash-output-guardium ./${UC_OPENSOURCE_ROOT_DIR}/filter-plugin/logstash-output-guardium
chmod -R 777 ${UC_OPENSOURCE_ROOT_DIR}

# Run UC build container to build plugins inside it
docker run -e UC_OPENSOURCE_ROOT_DIR=${UC_OPENSOURCE_ROOT_DIR} --name="Alan" -v `pwd`:`pwd` -w `pwd` -dit guc_dit:latest bash
chmod -R 755 **/gradlew
docker exec Alan bash -c "./buildUCPluginGems.sh"
if [ $? -eq 0 ]
then
  echo "Successfully tested and built"
else
  echo "Failed to test and build plugins"
  exit 1
fi
