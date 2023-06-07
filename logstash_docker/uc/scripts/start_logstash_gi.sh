#!/bin/bash
source ${UC_SCRIPTS}/utils.sh
source ${UC_SCRIPTS}/set_uc_log_level.sh
source ${UC_SCRIPTS}/create_keystore.sh

java -jar setup/uc-bootstrap.jar

logstash_pid=$(/usr/share/logstash/scripts/get_logstash_pid.sh)
if [[ -z "$logstash_pid" ]]; then
  # Aggregate env vars
    export GI_PIPELINE_DIR="${UC_ETC}/pipeline/"
    echo "Setting GI_PIPELINE_DIR to: ${GI_PIPELINE_DIR}"
    export GI_PLUGINS_DIR="${UC_ETC}/config/"
    echo "Setting GI_PLUGINS_DIR to: ${GI_PLUGINS_DIR}"
    export GI_PLUGINS_BINARIES_DIR="${GI_PLUGINS_DIR}binaries/"
    echo "Setting GI_PLUGINS_BINARIES_DIR to: ${GI_PLUGINS_BINARIES_DIR}"
    export GI_PLUGINS_DEPENDENCIES_DIR="${GI_PLUGINS_DIR}dependencies/"
    echo "Setting GI_PLUGINS_DEPENDENCIES_DIR to: ${GI_PLUGINS_DEPENDENCIES_DIR}"
	  ${UC_SCRIPTS}/install_customer_plugins.sh
	  ${UC_SCRIPTS}/install_customer_dependencies.sh

    setUcLogLevel "$UC_LOG_LEVEL"

    # TODO: change output plugin to get SniffersConfig.json from env variables
    echo "[{\"ip\":\"${MINI_SNIF_HOSTNAME}\", \"port\":\"${MINI_SNIF_PORT}\", \"isSSL\":\"${MINI_SNIF_SSL_ENABLED}\" }]" > ${UC_ETC}/SniffersConfig.json

	export UC_CONTAINER_UNIQUE_ID=$(echo ${HOSTNAME} | sed 's/.*_//')
	updateFromEnv "$UC_CONTAINER_UNIQUE_ID" "UC_CONTAINER_UNIQUE_ID"  ${UC_ETC}/UniversalConnector.json "\"containerUniqueId\":.*" "\"containerUniqueId\":\"$UC_CONTAINER_UNIQUE_ID\","
	echo "Replacing  UC_CONTAINER_UNIQUE_ID to: ${UC_CONTAINER_UNIQUE_ID}"
  
    # Replace pipelines.yml:
    echo "- pipeline.id: customer_pipeline" > ${UC_ETC}/pipelines.yml
    echo "  path.config: \"${GI_PIPELINE_DIR}\"" >> ${UC_ETC}/pipelines.yml
    echo "  queue.type: \${GUC_PERSISTENT_QUEUE_TYPE:memory}" >> ${UC_ETC}/pipelines.yml
    echo "  pipeline.ecs_compatibility: disabled" >> ${UC_ETC}/pipelines.yml

    # Start logstash
    exec logstash -l ${LOG_GUC_DIR} 2>&1
else
    echo "Logstash is already running..."
fi
