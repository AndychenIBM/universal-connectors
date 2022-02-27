#!/bin/bash
source ${UC_SCRIPTS}/utils.sh
source ${UC_SCRIPTS}/set_uc_log_level.sh

logstash_pid=$(/usr/share/logstash/scripts/get_logstash_pid.sh)
if [[ -z "$logstash_pid" ]]; then
    if [ -n GI_MODE ]
    then
        if [[ "$GI_MODE" = "true" ]]; then
            echo "run gi mode" >> /usr/share/logstash/mode.log
            /usr/share/logstash/scripts/start_logstash_gi.sh
            exit 0
        fi
    fi
    echo "run non gi mode"  >> /usr/share/logstash/mode.log
    #Change Sniffer IP to host's IP
    SNIF_IP=$( hostname -i | awk '{print $1}')
    updateFromEnv "$SNIF_IP" "Sniffer IP address" $UC_ETC/SniffersConfig.json "127.0.0.1" "$SNIF_IP"

    #Change log4j2uc.properties log level if needed
    setUcLogLevel "$UC_LOG_LEVEL"

    #Change connectorId if needed
    updateFromEnv "$CONNECTOR_ID" "CONNECTOR_ID" $UC_ETC/UniversalConnector.json "\"connectorId\":.*" "\"connectorId\":\"$CONNECTOR_ID\","

    setJVMParameters

    #Copy keystore, if exists:
    if [[ -e ${SSL_DIR}/logstash.keystore ]]; then 
        cp ${SSL_DIR}/logstash.keystore ${LOGSTASH_DIR}/config/
    fi
    
    #Start logstash
    logstash -b 80 -u 500 -l ${LOG_GUC_DIR} 2>&1 | tee -a ${LOG_GUC_DIR}/logstash_stdout_stderr.log
else
    echo "Logstash is already running..."
fi
