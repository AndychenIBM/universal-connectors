#!/bin/bash

#Save arguments
format=$1
path=$2
address=$3
logfile=$4

printf "%s: Params passed to filebeat script:\n\tFORMAT=%s\n\tPATH=%s\n\tADDRESS=%s\n\tLOG=%s\n" $(date +"%Y-%m-%dT%H:%M:%SZ") $format "$path" $address $logfile|& tee -a $logfile
#Find filebeat configuration file path
filebeat_conf=$( readlink  -f /*/filebeat/filebeat.yml )
printf "%s: filebeat configuration file path is: %s\n" $(date +"%Y-%m-%dT%H:%M:%SZ") $filebeat_conf|& tee -a $logfile

#Configure Filebeat input:
startRange=$(awk '/inputs/{ print NR; exit }' $filebeat_conf)
endRange=$((startRange+30))
sed -i -r "$startRange,$endRange{h;s/enabled: false.*/enabled: true/g}" $filebeat_conf

if grep -qF "$path" $filebeat_conf;then
	printf "%s: path is already located in filebeat configuration. Please check %s\n" $(date +"%Y-%m-%dT%H:%M:%SZ") $filebeat_conf|& tee -a $logfile
else
    sed -i -r "$startRange,$endRange{h;s/paths:.*/paths:\n    - ${path//\//\\/}/g}" $filebeat_conf
fi

#Configure Filebeat output:
sed -i -r "s/#output.logstash:/output.logstash:/g" $filebeat_conf
startRange=$(awk '/logstash/{ print NR; exit }' $filebeat_conf)
endRange=$((startRange+30))
if grep -qF "$address" $filebeat_conf;then
	printf "%s: Address is already located in filebeat configuration. Please check %s\n" $(date +"%Y-%m-%dT%H:%M:%SZ") $filebeat_conf|& tee -a $logfile
else
    sed -i -r "$startRange,$endRange{h;s/#hosts:.*/hosts: [\"$address\"]/g}" $filebeat_conf
fi

service rsyslog restart
printf "%s: Restarted filebeat.\n" $(date +"%Y-%m-%dT%H:%M:%SZ") |& tee -a $logfile
