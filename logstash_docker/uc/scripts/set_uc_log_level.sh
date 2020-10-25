#!/bin/bash
source ${UC_SCRIPTS}/utils.sh

function setUcLogLevel(){
    #Change log4j2uc.properties log level if needed
    if [[ "$1" =~ ^(all|debug|info|warn|error|fatal|off|trace)$ ]]; then

        export UC_LOG_LEVEL=$1
        updateFromEnv "$UC_LOG_LEVEL" "UC_LOG_LEVEL" $UC_ETC/log4j2uc.properties "logger.guardium.level = \w+" "logger.guardium.level = $UC_LOG_LEVEL"
        updateFromEnv "$UC_LOG_LEVEL" "UC_LOG_LEVEL" $UC_ETC/log4j2uc.properties "logger.logstashplugins.level = \w+" "logger.logstashplugins.level = $UC_LOG_LEVEL"
    fi
}

setUcLogLevel ${1}
