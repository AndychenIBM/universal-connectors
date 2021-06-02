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
    echo "[{\"ip\":\"${MINI_SNIF_HOSTNAME}\", \"port\":\"${MINI_SNIF_PORT}\", \"isSSL\":\"${MINI_SNIF_SSL_ENABLED}\" }]" > ${UC_ETC}/SniffersConfig.json

    # Replace pipelines.yml:
    echo "- pipeline.id: customer_pipeline" > ${UC_ETC}/pipelines.yml
    echo "  path.config: \"${GI_PIPELINE_DIR}\"" >> ${UC_ETC}/pipelines.yml
    echo "  queue.type: \${GUC_PERSISTENT_QUEUE_TYPE:memory}" >> ${UC_ETC}/pipelines.yml

    # Start logstash
    logstash --config.reload.automatic -l ${LOG_GUC_DIR} 2>&1 | tee -a ${LOG_GUC_DIR}/logstash_stdout_stderr.log
else
    echo "Logstash is already running..."
fi
