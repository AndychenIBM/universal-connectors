#!/bin/bash
source ${UC_SCRIPTS}/utils.sh
source ${UC_SCRIPTS}/set_uc_log_level.sh

logstash_pid=$(/usr/share/logstash/scripts/get_logstash_pid.sh)
if [[ -z "$logstash_pid" ]]; then
	${UC_SCRIPTS}/install_customer_plugins.sh

    setUcLogLevel "$UC_LOG_LEVEL"

    # TODO: change output plugin to get SniffersConfig.json from env variables
    rm -rf /usr/share/logstash/config/SniffersConfig.json
    echo "[{\"ip\":\"172.30.181.176\", \"port\":\"16023\", \"isSSL\":\"true\" }]" > ${UC_ETC}/SniffersConfig.json

    # Start logstash
    logstash -f ${LOGSTASH_DIR}/pipeline/gi_dummy.conf -l ${LOG_GUC_DIR} 2>&1 | tee -a ${LOG_GUC_DIR}/logstash_stdout_stderr.log
else
    echo "Logstash is already running..."
fi
