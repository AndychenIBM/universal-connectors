#!/bin/bash

FILE=${LOGSTASH_DIR}/customer/config/logstash-offline-plugins-7.5.0.zip
if test -f "$FILE"; then
    echo "preparing to install logstash-offline-plugins..."
    logstash-plugin install file:///$FILE
    echo "logstash-offline-plugins package is now installed and ready to use"
else
    echo "no logstash-offline-plugins zip file was found in plugins directory..."
fi
