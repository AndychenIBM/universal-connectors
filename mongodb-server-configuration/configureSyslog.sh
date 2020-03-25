#!/bin/bash

#Load params from configuration file
protocol=$1
address=$2
logfile=$3
#printf "%s: Params passed to syslog script:\n\tPROTOCOL=%s\n\tADDRESS=%s\n\tLOG=%s\n" $(date +"%Y-%m-%dT%H:%M:%SZ") $protocol $address $logfile|& tee -a $logfile

#Find rsyslog configuration file path
rsyslog_conf=$( readlink  -f /*/rsyslog.conf )
printf "%s: rsyslog configuration file path is: %s\n" $(date +"%Y-%m-%dT%H:%M:%SZ") $rsyslog_conf|& tee -a $logfile

if grep -qF "$address" $rsyslog_conf;then
	printf "%s: Address is already located in rsyslog configuration. Please check %s\n" $(date +"%Y-%m-%dT%H:%M:%SZ") $rsyslog_conf|& tee -a $logfile

else
    last_programname_appearance=$(cat -n $rsyslog_conf | grep "programname" | tail -1 |  cut -f 1)
	if [ -z "$last_programname_appearance" ];then
		last_programname_appearance=$(cat -n $rsyslog_conf | grep "#### MODULES ####" | tail -1 |  cut -f 1)
	fi
	last_programname_appearance=$((last_programname_appearance+1))
	last_programname_appearance+="i"
	echo last_programname_appearance
	if [[ "$protocol" == "TCP" ]]; then
		new_line=":programname, isequal, \"mongod\" @@$address"
	else #default- UDP
		new_line=":programname, isequal, \"mongod\" @$address"
	fi

	sed -i "$last_programname_appearance$new_line" $rsyslog_conf
	printf "%s: Inserted new line to rsyslog configuration file:%s\n" $(date +"%Y-%m-%dT%H:%M:%SZ") $new_line|& tee -a $logfile

	service rsyslog restart
	printf "%s: Restarted rsyslog.\n" $(date +"%Y-%m-%dT%H:%M:%SZ") |& tee -a $logfile
fi
