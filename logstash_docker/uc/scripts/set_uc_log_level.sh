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

#Change log4j2uc.properties log level if needed
if [[ "$1" =~ ^(all|debug|info|warn|error|fatal|off|trace)$ ]]; then

    export UC_LOG_LEVEL=$1
    updateFromEnv "$UC_LOG_LEVEL" "UC_LOG_LEVEL" $UDS_ETC/log4j2uc.properties "filter.threshold.level = \w+" "filter.threshold.level = $UC_LOG_LEVEL"
    updateFromEnv "$UC_LOG_LEVEL" "UC_LOG_LEVEL" $UDS_ETC/log4j2uc.properties "logger.guardium.level = \w+" "logger.guardium.level = $UC_LOG_LEVEL"
    updateFromEnv "$UC_LOG_LEVEL" "UC_LOG_LEVEL" $UDS_ETC/log4j2uc.properties "logger.logstashplugins.level = \w+" "logger.logstashplugins.level = $UC_LOG_LEVEL"
fi
