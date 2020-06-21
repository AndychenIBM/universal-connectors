#!/bin/bash

home_dir="/root/mongodb-server-configuration"
logfile="${home_dir}/configureServer.log"
#Load params from configuration file
configfile="${home_dir}/configureServer.conf"
#dest=$(grep "destination" $configfile | cut -d ':' -f 2)
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
      echo "1. Configure Mongodb to send audit logs to Syslog or Filebeat"
      echo "2. Configure Syslog/Filebeat to send logs to Guardium Universal Connector"
      echo "Mandatory arguments:"
      echo "syslog/file"
      echo "Mandatory flags:"
      echo "-h, --help                Show brief help"
      echo "-f, --filter	          Specify a filter for mongodb auditLog"
	  echo "-a, --address       	  Specify an address to send data <ip_address>:<port>"
	  echo "-p, --protocol       	  Specify the protocol for communication with Guardium Universal Connector: TCP or UDP"
	  echo "--restart-mongodb      	  Changes MongoDB configuration file, DB restart will be performed."
      exit 0
      ;;
     syslog|file)
        dest=$1
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
    --restart-mongodb)
      restart_mongodb='true';
      shift
      ;;
    *)
      break
      ;;
  esac
done

if [[ -z $dest ]];
then
	echo "Destination argument is mandatory. Please define syslog or file"
	exit 1
fi

printf "%s: mongod params:\n\tDEST=%s\n\tFORMAT=%s\n\tPATH=%s\n\tFILTER=%s\n" $(date +"%Y-%m-%dT%H:%M:%SZ") $dest $format "$path" "$filter" |& tee -a $logfile

if [ $restart_mongodb ];
then
	echo "Configuring MongoDB auditLog..."
	bash $home_dir/linux/configureMongodb.sh $dest $format $path "$filter" $logfile
fi

if [ "$dest" = "syslog" ];
then
    printf "%s: rsyslog params:\n\tPROTOCOL=%s\n\tADDRESS=%s\n" $(date +"%Y-%m-%dT%H:%M:%SZ") $protocol $address |& tee -a $logfile
    printf "%s: Configuring syslog...\n" $(date +"%Y-%m-%dT%H:%M:%SZ") |& tee -a $logfile
    bash $home_dir/linux/configureSyslog.sh "$protocol" "$address" "$logfile"
elif [ "$dest" = "file" ];
then
    printf "%s: Configuring filebeat...\n" $(date +"%Y-%m-%dT%H:%M:%SZ") |& tee -a $logfile
    bash $home_dir/linux/configureFilebeat.sh "$format" "$path" "$address" "$logfile"
fi

printf "%s: Done configuring Server.\n" $(date +"%Y-%m-%dT%H:%M:%SZ") |& tee -a $logfile
