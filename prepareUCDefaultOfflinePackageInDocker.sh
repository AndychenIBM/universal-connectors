CURDIR=$(pwd)
function installPlugin() {
  line=$1
  if [[ $1 =~ ^#.* ]]; then
    echo "Ignoring the commented out following line: $1"
  # TODO- remove output_to_guardium handling after turning it to an offline-package
  elif [[ "$1" == *"output_to_guardium"* ]]; then
    echo "Ignoring output_to_guardium plugin installation inside UC default offline package."
  else
    pluginName=${1##*/}
    echo "installing ${pluginName}"
    ../bin/logstash-plugin install "${pluginName}"
    packages_list+=" ${pluginName%-*}"
  fi
}

cp logstash_docker/uc/config/*.gem defaultOfflinePackagePlugins.txt /usr/share/logstash/config
cd /usr/share/logstash/config
while read line; do installPlugin ${UC_OPENSOURCE_ROOT_DIR}/$line; done <defaultOfflinePackagePlugins.txt

#TODO- remove hardcoded installation of cloudwatch_logs after publishing it to open-source Github repo
installPlugin logstash_docker/uc/config/logstash-input-cloudwatch_logs-1.0.3.gem

echo "packages included in the offline package: $packages_list"
../bin/logstash-plugin prepare-offline-pack --output ./guardium_logstash-offline-plugins.zip --overwrite $packages_list
../bin/logstash-plugin list --verbose
cp ./guardium_logstash-offline-plugins.zip $CURDIR/logstash_docker/uc/config/
