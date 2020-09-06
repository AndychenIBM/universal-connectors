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