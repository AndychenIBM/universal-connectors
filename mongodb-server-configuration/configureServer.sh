#!/bin/bash

logfile="/root/mongodb-server-configuration/configureServer.log"
#Load params from configuration file
configfile='/root/mongodb-server-configuration/configureServer.conf'
dest=$(grep "destination" $configfile | cut -d ':' -f 2)
format=$(grep "format" $configfile | cut -d ':' -f 2)
path=$(grep "path" $configfile | cut -d ':' -f 2)
filter=$(grep "filter" $configfile | cut -d ':' -f2-)
protocol=$(grep "protocol" $configfile | cut -d ':' -f 2)
protocol=${protocol^^}
address=$(grep "address" $configfile | cut -d ':' -f2-)
printf "%s: mongod params:\n\tDEST=%s\n\tFORMAT=%s\n\tPATH=%s\n\tFILTER=%s\n" $(date +"%Y-%m-%dT%H:%M:%SZ") $dest $format $path "$filter" |& tee -a $logfile
printf "%s: rsyslog params:\n\tPROTOCOL=%s\n\tADDRESS=%s\n" $(date +"%Y-%m-%dT%H:%M:%SZ") $protocol $address |& tee -a $logfile

if [ "$1" != "syslog" ];
then
	echo "Configuring MongoDB auditLog..."
	bash configureMongodb.sh $dest $format $path "$filter" $logfile
fi
printf "%s: Configuring syslog...\n" $(date +"%Y-%m-%dT%H:%M:%SZ") |& tee -a $logfile
bash configureSyslog.sh "$protocol" "$address" "$logfile"
printf "%s: Done configuring Server.\n" $(date +"%Y-%m-%dT%H:%M:%SZ") |& tee -a $logfile