# TODO: adjust script to bash and not zsh
./runPluginsTests.sh
pluginNamesString=""
gemsListString=""

function addGemNameToString(){
  pluginDir=$(echo "$1" | cut -d ";" -f 1)
  pluginName=$(echo "$1" | cut -d ";" -f 2)

  pluginNamesString+="${pluginName} "
  # ignore cloud-logs plugin when looking for gem files
  [[ "${pluginDir}" = "logstash-input-cloudwatch-logs-master" ]] && return 0
  for file in "${pluginDir}"/*.gem; do
    gemsListString+="${file} "
  done
}

grep -v '^#' defaultOfflinePackagePlugins.txt | while read -r line ; do addGemNameToString "$line"; done
# Manually add cloudwatch-logs plugin
gemsListString+="logstash_docker/uc/config/logstash-input-cloudwatch_logs-1.0.3.gem"
echo "Installing the following plugins: ${gemsListString}"
docker exec -it Alan bash -c "/usr/share/logstash/bin/logstash-plugin install --local --no-verify ${gemsListString}"

echo "Preparing default offline-package for Universal-Connector with the following plugins: ${pluginNamesString}"
docker exec -it Alan bash -c "/usr/share/logstash/bin/logstash-plugin prepare-offline-pack --output ./guardium_default_plugins.zip --overwrite ${pluginNamesString}"

mv guardium_default_plugins.zip logstash_docker/uc/config/.
exit 0