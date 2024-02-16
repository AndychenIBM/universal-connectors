#!/bin/bash
source ${UC_SCRIPTS}/utils.sh
source ${UC_SCRIPTS}/set_uc_log_level.sh
source ${UC_SCRIPTS}/create_keystore.sh

# Check if BOOTSTRAP_PATH is set
if [[ -z "$BOOTSTRAP_PATH" ]]; then
    BOOTSTRAP_PATH="setup/uc-bootstrap.jar"
fi

java -jar $BOOTSTRAP_PATH
exit_code=$?

if [[ $exit_code -eq 0 ]]; then
    echo "uc-bootstrap ran successfully"
elif [[ $exit_code -eq 2 ]]; then
    echo "uc-bootstrap failed to generate jwt token for tenant: ${TENANT_ID}."
    exit 2
elif [[ $exit_code -eq 3 ]]; then
    echo "uc-bootstrap failed to get setup information from universal-connector-manager for tenant: ${TENANT_ID}, connection: ${INPUT_PLUGIN_ID}."
    export TROUBLESHOOTING="Missing configurations"
else
  echo "uc-bootstrap exit code: $exit_code"
  export TROUBLESHOOTING="Missing configurations"
fi


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

    # If no errors in initialization then start logstash
    if [[ -z "$TROUBLESHOOTING" ||  "$TROUBLESHOOTING" = "OK" ]]; then
      logstash -l ${LOG_GUC_DIR} 2>&1
      logstash_exit_code=$?
      echo "logstash exit code: $logstash_exit_code"

      output_file=${LOG_GUC_DIR}troubleshooting_output.txt
      # Check the exit code and continue with the next line if needed
      if [[ -e "$output_file" && -s "$output_file" ]]; then
         echo "Universal connector ${INPUT_PLUGIN_ID} of tenant ${TENANT_ID} failed because of an error: $(cat $output_file)"
      else
         # run troubleshooting and prepare new pipeline to send to ucm the error, logstash has failed = true
         ${UC_SCRIPTS}/send_errors_to_kafka.sh true
         echo "Universal connector ${INPUT_PLUGIN_ID} of tenant ${TENANT_ID} failed because of an error: $(cat $output_file)"
         echo "Starting new logstash pipeline to send ucm the error message"
         # start logstash again to send ucm the error message
         logstash -l ${LOG_GUC_DIR} 2>&1
      fi
    fi

else
    echo "Logstash is already running..."
fi

# if we got here then liveness found an error in logstash or logstash had an exception.
exit 1