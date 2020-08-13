#!/bin/bash

function updateFromEnv(){
    ENV_VAR=$1
    VAR_NAME=$2
    FILE=$3
    ORIGINAL_STRING=$4
    REPLACE_STRING=$5

    if [ -z "$ENV_VAR" ]
    then
        echo "No $VAR_NAME was set as an environment variable. Using default value."
    else
        sed -i -r "s/$ORIGINAL_STRING/$REPLACE_STRING/g" $FILE
        echo "$VAR_NAME was set to $ENV_VAR"
    fi
}

logstash_pid=$(/usr/share/logstash/scripts/get_logstash_pid.sh)
if [[ -z "$logstash_pid" ]]; then
    #Change Sniffer IP to host's IP
    SNIF_IP=$(hostname -i)
    updateFromEnv "$SNIF_IP" "Sniffer IP address" $UDS_ETC/SniffersConfig.json "127.0.0.1" "$SNIF_IP"

    #Change log4j2uc.properties log level if needed
    if [[ "$UC_LOG_LEVEL" =~ ^(all|debug|info|warn|error|fatal|off|trace)$ ]]; then
        updateFromEnv "$UC_LOG_LEVEL" "UC_LOG_LEVEL" $UDS_ETC/log4j2uc.properties "filter.threshold.level = error" "filter.threshold.level = $UC_LOG_LEVEL"
        updateFromEnv "$UC_LOG_LEVEL" "UC_LOG_LEVEL" $UDS_ETC/log4j2uc.properties "logger.guardium.level = error" "logger.guardium.level = $UC_LOG_LEVEL"
        updateFromEnv "$UC_LOG_LEVEL" "UC_LOG_LEVEL" $UDS_ETC/log4j2uc.properties "logger.logstashplugins.level = error" "logger.logstashplugins.level = $UC_LOG_LEVEL"
    fi

    #Change connectorId if needed
    updateFromEnv "$CONNECTOR_ID" "CONNECTOR_ID" $UDS_ETC/UniversalConnector.json "\"connectorId\":.*" "\"connectorId\":\"$CONNECTOR_ID\","


    #Change filebeat/syslog ports if needed
    #Note that this option is only relevant when using mongodb-syslog-filebeat.conf as a configuration file for logstash
    MONGODB_CONF=${LOGSTASH_DIR}/pipeline/mongodb-syslog-filebeat.conf
    updateFromEnv "$FILEBEAT_PORT" "FILEBEAT_PORT" $MONGODB_CONF "beats \{ port => 5044 type => filebeat" "beats \{ port => $FILEBEAT_PORT type => filebeat"
    updateFromEnv "$UDP_PORT" "UDP_PORT" $MONGODB_CONF "udp \{ port => 5141 type => syslog" "udp \{ port => $UDP_PORT type => syslog"
    updateFromEnv "$TCP_PORT" "TCP_PORT" $MONGODB_CONF "tcp \{ port => 5000 type => syslog" "tcp \{ port => $TCP_PORT type => syslog"

    #Start logstash
    logstash
else
    echo "Logstash is already running..."
fi
