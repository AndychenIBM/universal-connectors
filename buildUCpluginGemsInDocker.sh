#!/bin/bash
UC_PLUGIN_REPO_BRANCH=main
UC_PLUGINS_BUILD_FOLDER=universal-connectors-${UC_PLUGIN_REPO_BRANCH}/build
function force_copy_if_exists() {
  if [ -e ${UC_PLUGINS_BUILD_FOLDER}/$1 ]
then
  echo "Copying ${1} from open-source"
  cp -rf ${UC_PLUGINS_BUILD_FOLDER}/$1 .
else
  echo "No external file found ${UC_PLUGINS_BUILD_FOLDER}/${1}. Using local copy"
fi
}

chmod -R 777 *
cd test
docker build -t guc_dit:latest .
cd ..

# Pull UC plugins from open-source repo
rm -rf universal-connectors-${UC_PLUGIN_REPO_BRANCH} ${UC_PLUGIN_REPO_BRANCH}.zip universal-connectors-${UC_PLUGIN_REPO_BRANCH}
wget https://github.com/IBM/universal-connectors/archive/refs/heads/${UC_PLUGIN_REPO_BRANCH}.zip && unzip -q ${UC_PLUGIN_REPO_BRANCH}.zip
cp -R ./logstash-output-guardium ./universal-connectors-${UC_PLUGIN_REPO_BRANCH}/filter-plugin/logstash-output-guardium
chmod -R 777 universal-connectors-${UC_PLUGIN_REPO_BRANCH}

# copy lists of plugins to build and package in UC default offline-package
force_copy_if_exists pluginsToBuild.txt
force_copy_if_exists defaultOfflinePackagePlugins.txt

# Run UC build container to build plugins inside it
docker run  --name="Alan" -v `pwd`:`pwd` -w `pwd` -dit guc_dit:latest bash
chmod -R 755 **/gradlew
docker exec Alan bash -c "./buildUCPluginGems.sh"
if [ $? -eq 0 ]
then
  echo "Successfully tested and built"
else
  echo "Failed to test and build plugins"
  exit 1
fi
