#!/bin/bash
source ${UC_SCRIPTS}/utils.sh
source ${UC_SCRIPTS}/set_uc_log_level.sh

logstash_pid=$(/usr/share/logstash/scripts/get_logstash_pid.sh)
if [[ -z "$logstash_pid" ]]; then
  # Aggregate env vars
    export GI_PIPELINE_DIR="${GUC_PIPELINE_CONFIG_PATH}/${INPUT_PLUGIN_ID}/pipeline/"
    echo "Setting GI_PIPELINE_DIR to: ${GI_PIPELINE_DIR}"
    export GI_PLUGINS_DIR="${GUC_PIPELINE_CONFIG_PATH}/${INPUT_PLUGIN_ID}/config/"
    echo "Setting GI_PLUGINS_DIR to: ${GI_PLUGINS_DIR}"

	  ${UC_SCRIPTS}/install_customer_plugins.sh

    setUcLogLevel "$UC_LOG_LEVEL"

    # TODO: change output plugin to get SniffersConfig.json from env variables
    rm -rf /usr/share/logstash/config/SniffersConfig.json
    echo "[{\"ip\":\"${MINI_SNIF_HOSTNAME}\", \"port\":\"${MINI_SNIF_PORT}\", \"isSSL\":\"${MINI_SNIF_SSL_ENABLED}\" }]" > ${UC_ETC}/SniffersConfig.json

    # Remove 3 first lines (default on-prem pipeline) from pipelines.yml
    sed -i -e 1,3d ${UC_ETC}/pipelines.yml

    # Aggregate env vars
    export GI_PIPELINE_DIR="${GUC_PIPELINE_CONFIG_PATH}/${TENANT_ID}/${CLUSTER_ID}/pipeline/"
    export GI_PLUGINS_DIR="${GUC_PIPELINE_CONFIG_PATH}/${TENANT_ID}/${CLUSTER_ID}/config/"

    # Start logstash
    logstash --config.reload.automatic -l ${LOG_GUC_DIR} 2>&1 | tee -a ${LOG_GUC_DIR}/logstash_stdout_stderr.log
else
    echo "Logstash is already running..."
fi
