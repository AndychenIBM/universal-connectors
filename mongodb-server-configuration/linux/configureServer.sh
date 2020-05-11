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

#Load flags
while test $# -gt 0; do
  case "$1" in
    -h|--help)
      echo "This script has 2 purposes:"
      echo "1. Configure Mongodb to send audit logs to Syslog"
      echo "2. Configure Syslog to send logs to Guardium Universal Connector"
      echo "Optional flags:"
      echo "-h, --help                Show brief help"
      echo "-d, --destination         Specify a destination for mongodb: syslog or file"
      echo "-f, --filter	          Specify a filter for mongodb auditLog"
	  echo "-a, --address       	  Specify an address to send data <ip_address>:<port>"
	  echo "-p, --protocol       	  Specify the protocol for communication with Guardium Universal Connector: TCP or UDP"
	  echo "--syslog-only       	  Update only syslog configuration file. No mongo DB changes, no DB restart required."
      exit 0
      ;;
    -d|--destination)
      shift
      if test $# -gt 0; then
        dest=$1
      else
        echo "no destination specified"
        exit 1
      fi
      shift
      ;;
	-f|--filter)
      shift
      if test $# -gt 0; then
        filter=$1
      else
        echo "no filter specified"
        exit 1
      fi
      shift
      ;;
    -a|--address)
      shift
      if test $# -gt 0; then
        address=$1
      else
        echo "no address specified"
        exit 1
      fi
      shift
      ;;
    -p|--protocol)
      shift
      if test $# -gt 0; then
        protocol=$1
      else
        echo "no protocol specified"
        exit 1
      fi
      shift
      ;;
	--syslog-only)
      syslog_only='true';
      shift
      ;;
    *)
      break
      ;;
  esac
done

printf "%s: mongod params:\n\tDEST=%s\n\tFORMAT=%s\n\tPATH=%s\n\tFILTER=%s\n" $(date +"%Y-%m-%dT%H:%M:%SZ") $dest $format $path "$filter" |& tee -a $logfile
printf "%s: rsyslog params:\n\tPROTOCOL=%s\n\tADDRESS=%s\n" $(date +"%Y-%m-%dT%H:%M:%SZ") $protocol $address |& tee -a $logfile

if [ -z $syslog_only ];
then
	echo "Configuring MongoDB auditLog..."
	bash configureMongodb.sh $dest $format $path "$filter" $logfile
fi
printf "%s: Configuring syslog...\n" $(date +"%Y-%m-%dT%H:%M:%SZ") |& tee -a $logfile
bash configureSyslog.sh "$protocol" "$address" "$logfile"
printf "%s: Done configuring Server.\n" $(date +"%Y-%m-%dT%H:%M:%SZ") |& tee -a $logfile
echo "%s: Done configuring Server.\n" $(date +"%Y-%m-%dT%H:%M:%SZ")
