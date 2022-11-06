CURDIR=$(pwd)
function addPluginToInstallationList() {
  line=$1
  if [[ $1 =~ ^#.* ]]; then
    echo "Ignoring the commented out following line: $1"
  else
    pluginName=${1##*/}
    packages_list+=" ${pluginName%-*}"
  fi
}

function removeUninstalledPluginFromPackagingList() {
  installedPlugins=$(ls | grep gem)
  for value in $packages_list
  do
    if [[ ${installedPlugins} != *"${value}"* ]];then
      echo "${value} gem file was not found in the plugins directory. Removing it from default offline-package for UC"
      packages_list=${packages_list//$value/}
    fi
  done
}

cp logstash_docker/uc/config/*.gem defaultOfflinePackagePlugins.txt /usr/share/logstash/config
cd /usr/share/logstash/config

while read line; do addPluginToInstallationList ${UC_OPENSOURCE_ROOT_DIR}/$line; done <defaultOfflinePackagePlugins.txt

addPluginToInstallationList ${UC_OPENSOURCE_ROOT_DIR}/filter-plugin/logstash-output-guardium/logstash-output-guardium-1.4.0.gem


#TODO- remove hardcoded installation of cloudwatch_logs after publishing it to open-source Github repo
addPluginToInstallationList logstash_docker/uc/config/logstash-input-cloudwatch_logs-1.0.3.gem

echo "Installing packages on Logstash..."
../bin/logstash-plugin install *.gem

removeUninstalledPluginFromPackagingList

echo "Packages included in UC default offline package: $packages_list"
../bin/logstash-plugin prepare-offline-pack --output ./guardium_logstash-offline-plugins.zip --overwrite $packages_list
cp ./guardium_logstash-offline-plugins.zip $CURDIR/logstash_docker/uc/config/
