#!/bin/bash

if [[ "$UC_LOG_LEVEL" =~ ^(ALL|DEBUG|INFO|WARN|ERROR|FATAL|OFF|TRACE)$ ]]; then
    sed -i -r "s/ERROR/$UC_LOG_LEVEL/g" $UDS_ETC/log4j.properties
    echo "UC_LOG_LEVEL was set to $UC_LOG_LEVEL"
fi
logstash
