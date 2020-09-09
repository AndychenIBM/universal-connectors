#!/bin/bash
logstash_pid=$(bash ${UC_SCRIPTS}/get_logstash_pid.sh)
if [[ -z $logstash_pid ]];
then
    echo "logstash is not running"
else
    kill -SIGHUP $logstash_pid
fi