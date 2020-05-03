#!/bin/bash

#save arguments
dest=$1
format=$2
path=$3
filter=$4
logfile=$5
#printf "%s: Params passed to mongod script:\nDESTINATION=%s\n\tFORMAT=%s\n\tPATH=%s\n\tFILTER=%s\n\tLOG=%s\n" $(date +"%Y-%m-%dT%H:%M:%SZ") $dest $format $path "$filter" $logfile|& tee -a $logfile

#Find mongod pid if exists
mon_pid=$(service mongod status|grep -Eo '[0-9]+')
printf "%s: Mongod pid: %s.\n" $(date +"%Y-%m-%dT%H:%M:%SZ") $mon_pid|& tee -a $logfile

#Find mongod configuration file path
mongod_conf=$( readlink  -f /*/mongod.conf )
printf "%s: Mongod configuration file path is: %s.\n" $(date +"%Y-%m-%dT%H:%M:%SZ") $mongod_conf|& tee -a $logfile

#Backup configuration file
#current_time=$(date "+%Y%m%d-%H%M%S")
#cp $mongod_conf $mongod_conf.$current_time
#echo "created $mongod_conf.$current_time as a backup configuration file" |& tee -a $logfile

#Find mongod pid file path configuration
printf "%s: Mongod pid file path is:%s.\n" $(date +"%Y-%m-%dT%H:%M:%SZ") $(grep -o 'pidFilePath[^#]*' $mongod_conf | cut -d ':' -f 2)|& tee -a $logfile

#Find if audit section exists and set in mongod.conf
#If audit section exists , update audit destination to syslog and update filters, if it does not exist, add the audit section to mongod.conf"
if grep -lq  '^auditLog' $mongod_conf
	then
		printf "%s: AuditLog will be set to syslog in mongod.conf\n" $(date +"%Y-%m-%dT%H:%M:%SZ") |& tee -a $logfile
		startRange=$(awk '/auditLog/{ print NR; exit }' $mongod_conf)
		endRange=$((startRange+10))
		ignoreCommentsString="[^#]*//1;x;s/#.*//"
		sed -i -r "$startRange,$endRange{h;s/$ignoreCommentsString;s/destination:.*/destination: $dest/g;G;s/(.*)\n/\1/}" $mongod_conf
		sed -i -r "$startRange,$endRange{h;s/$ignoreCommentsString;s/format:.*/format: $format/g;G;s/(.*)\n/\1/}" $mongod_conf
		sed -i -r "$startRange,$endRange{h;s/$ignoreCommentsString;s/filter:.*/filter: $filter/g;G;s/(.*)\n/\1/}" $mongod_conf
		sed -i "s/#setParameter: {auditAuthorizationSuccess: true}/setParameter: {auditAuthorizationSuccess: true}/g" $mongod_conf
	else
		printf "%s: AuditLog section will be added to mongod.conf\n" $(date +"%Y-%m-%dT%H:%M:%SZ") |& tee -a $logfile
		printf "\nauditLog:\n  destination: $dest\n  filter: $filter\nsetParameter: {auditAuthorizationSuccess: true}  ">> $mongod_conf
fi

#Restart mongod service after configuration change
service mongod stop
rm -f /tmp/mongodb-27017.sock
sleep 2
if service mongod status|grep -q "stopped";
then
	service mongod start
	printf "%s: Restarted mongod.\n" $(date +"%Y-%m-%dT%H:%M:%SZ") |& tee -a $logfile
else
	printf "%s: Failed to stop mongodb.\n" $(date +"%Y-%m-%dT%H:%M:%SZ") |& tee -a $logfile
fi
#mongod --auth --port 27017