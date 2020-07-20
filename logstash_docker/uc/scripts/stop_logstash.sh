#!/bin/bash

logstash_pid=$(pgrep /opt/ibm/java/jre/bin/java)
if [[ -z $logstash_pid ]];
then
    echo "logstash is not running"
else
    kill $logstash_pid
fi