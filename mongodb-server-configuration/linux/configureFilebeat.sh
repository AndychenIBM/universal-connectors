#!/bin/bash

#Save arguments
format=$1
path=$2
host_addresses=${3//,/\"\,\"}
enable_loadbalance=$4
logfile=$5

printf "%s: Params passed to filebeat script:\n\tFORMAT=%s\n\tPATH=%s\n\tHOST_ADDRESSES=%s\n\tENABLE_LOADBALANCE=%s\n\tLOG=%s\n" $(date +"%Y-%m-%dT%H:%M:%SZ") $format "$path" $host_addresses $enable_loadbalance $logfile|& tee -a $logfile

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
if grep -qF "$host_addresses" $filebeat_conf;then
	printf "%s: All host addresses are already located in filebeat configuration. Please check %s\n" $(date +"%Y-%m-%dT%H:%M:%SZ") $filebeat_conf|& tee -a $logfile
else
	replace_string="hosts: [\"$host_addresses\"]"
	if [ "$enable_loadbalance" = 1 ]; then
		replace_string="${replace_string}\n  loadbalance: true"
	fi
	
	sed -i -r "$startRange,$endRange s/#hosts:.*/$replace_string/w tmp.txt" $filebeat_conf
	if [ -s tmp.txt ]; then
		printf "%s: Uncommented and updated host addresses in filebeat configuration.\n" $(date +"%Y-%m-%dT%H:%M:%SZ")|& tee -a $logfile
	else
		sed -i -r "$startRange,$endRange s/hosts:.*/$replace_string/w tmp.txt" $filebeat_conf
		if [ -s tmp.txt ]; then
			printf "%s: Updated host addresses in filebeat configuration.\n" $(date +"%Y-%m-%dT%H:%M:%SZ")|& tee -a $logfile
		else
			printf "%s: Could not find hosts field in logstash output section. please check filebeat configuration.\n" $(date +"%Y-%m-%dT%H:%M:%SZ")|& tee -a $logfile
		fi
	fi

	rm -rf tmp.txt
fi

service filebeat restart
printf "%s: Restarted filebeat.\n" $(date +"%Y-%m-%dT%H:%M:%SZ") |& tee -a $logfile
