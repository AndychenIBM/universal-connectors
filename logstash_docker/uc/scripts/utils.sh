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

function setJVMParameters(){
    TOTAL_MEM=$(free -g|grep Mem|awk '{print $2}')
    export LOGSTASH_HEAP_SIZE=$((TOTAL_MEM/10))
    if [ $LOGSTASH_HEAP_SIZE -gt 1 ]; then
        updateFromEnv "$LOGSTASH_HEAP_SIZE" "Xms" $UC_ETC/jvm.options "-Xms1g" "-Xms${LOGSTASH_HEAP_SIZE}g"
        updateFromEnv "$LOGSTASH_HEAP_SIZE" "Xmx" $UC_ETC/jvm.options "-Xmx1g" "-Xmx${LOGSTASH_HEAP_SIZE}g"
    fi
}