#!/bin/bash

#Change log4j log level if needed
if [[ "$UC_LOG_LEVEL" =~ ^(ALL|DEBUG|INFO|WARN|ERROR|FATAL|OFF|TRACE)$ ]]; then
    sed -i -r "s/ERROR/$UC_LOG_LEVEL/g" $UDS_ETC/log4j.properties
    echo "UC_LOG_LEVEL was set to $UC_LOG_LEVEL"
fi

#Change connectorId
if [ -z "$CONNECTOR_ID" ]
then
    echo "no connectorId was entered as an environment variable"
else
    sed -i -r "s/\"connectorId\":.*/\"connectorId\":\"$CONNECTOR_ID\"/g" $UDS_ETC/UniversalConnector.json
    echo "CONNECTOR_ID was set to $CONNECTOR_ID"
fi


#Start logstash
logstash
