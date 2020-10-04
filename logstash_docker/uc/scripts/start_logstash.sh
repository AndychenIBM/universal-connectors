#!/bin/bash
source ${UC_SCRIPTS}/utils.sh
source ${UC_SCRIPTS}/set_uc_log_level.sh

logstash_pid=$(/usr/share/logstash/scripts/get_logstash_pid.sh)
if [[ -z "$logstash_pid" ]]; then
    #Change Sniffer IP to host's IP
    SNIF_IP=$( hostname -i | awk '{print $1}')
    updateFromEnv "$SNIF_IP" "Sniffer IP address" $UDS_ETC/SniffersConfig.json "127.0.0.1" "$SNIF_IP"

    #Change log4j2uc.properties log level if needed
    setUcLogLevel "$UC_LOG_LEVEL"

    #Change connectorId if needed
    updateFromEnv "$CONNECTOR_ID" "CONNECTOR_ID" $UDS_ETC/UniversalConnector.json "\"connectorId\":.*" "\"connectorId\":\"$CONNECTOR_ID\","


    #Change filebeat/syslog ports if needed
    #Note that this option is only relevant when using mongodb-syslog-filebeat.conf as a configuration file for logstash
    MONGODB_CONF=${LOGSTASH_DIR}/pipeline/mongodb-syslog-filebeat.conf
    updateFromEnv "$FILEBEAT_PORT" "FILEBEAT_PORT" $MONGODB_CONF "beats \{ port => 5044 type => filebeat" "beats \{ port => $FILEBEAT_PORT type => filebeat"
    updateFromEnv "$UDP_PORT" "UDP_PORT" $MONGODB_CONF "udp \{ port => 5141 type => syslog" "udp \{ port => $UDP_PORT type => syslog"
    updateFromEnv "$TCP_PORT" "TCP_PORT" $MONGODB_CONF "tcp \{ port => 5000 type => syslog" "tcp \{ port => $TCP_PORT type => syslog"

    setJVMParameters

    #Start logstash
    logstash -b 50 -u 500 -l ${LOG_GUC_DIR} 2>&1 | tee -a ${LOG_GUC_DIR}/logstash_stdout_stderr.log
else
    echo "Logstash is already running..."
fi
