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

cp logstash_docker/uc/config/*.gem /usr/share/logstash/config
cd /usr/share/logstash/config

addPluginToInstallationList ${UC_OPENSOURCE_ROOT_DIR}/filter-plugin/logstash-output-guardium/logstash-output-guardium-1.4.3.gem
addPluginToInstallationList logstash_docker/uc/config/logstash-input-cloudwatch_logs-1.0.5.gem

echo "Installing packages on Logstash..."
../bin/logstash-plugin install *.gem

echo "Packages included in UC default offline package: $packages_list"
../bin/logstash-plugin prepare-offline-pack --output ./guardium_logstash-offline-plugins.zip --overwrite $packages_list
cp ./guardium_logstash-offline-plugins.zip $CURDIR/logstash_docker/uc/config/
