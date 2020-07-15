#!/bin/bash

home_dir=$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )
logfile="${home_dir}/../configureServer.log"
#Load params from configuration file
#configfile="${home_dir}/configureServer.conf"
##dest=$(grep "destination" $configfile | cut -d ':' -f 2)
#format=$(grep "format" $configfile | cut -d ':' -f 2)
#path=$(grep "path" $configfile | cut -d ':' -f 2)
#filter=$(grep "filter" $configfile | cut -d ':' -f2-)
#protocol=$(grep "protocol" $configfile | cut -d ':' -f 2)
#protocol=${protocol^^}
#address=$(grep "address" $configfile | cut -d ':' -f2-)

format="-"
path="-"
protocol="-"
restart_mongodb=0
enable_loadbalance=0

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
	  echo "--host-addresses       	  Specify addresses to send data seperated by comma. for example: <ip_address1>:<port1>,<ip_address2>:<port2>"
	  echo "-p, --protocol       	  Specify the protocol for communication with Guardium Universal Connector: TCP or UDP"
	  echo "--enable-loadbalance      Enable Filebeat loadbalance."
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
	  --host_addresses)
      shift
      if test $# -gt 0; then
        host_addresses=$1
      else
        echo "no addresses specified"
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
    --path)
      shift
      if test $# -gt 0; then
        path=$1
      else
        echo "no path specified"
        exit 1
      fi
      shift
      ;;
    --format)
      shift
      if test $# -gt 0; then
        format=$1
      else
        echo "no path specified"
        exit 1
      fi
      shift
      ;;
	--enable-loadbalance)
      enable_loadbalance=1;
      shift
      ;;
    --restart-mongodb)
      restart_mongodb=1;
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

printf "%s: mongod params:\n\tDEST=%s\n\tFORMAT=%s\n\tPATH=%s\n\tFILTER=%s\n\tRESTART_MONGODB=%s\n" $(date +"%Y-%m-%dT%H:%M:%SZ") $dest $format "$path" "$filter" $restart_mongodb|& tee -a $logfile

printf "Configuring MongoDB auditLog...\n" |& tee -a $logfile
bash $home_dir/configureMongodb.sh $dest $format $path "$filter" $restart_mongodb $logfile

if [ "$dest" = "syslog" ];
then
    printf "%s: Configuring syslog...\n" $(date +"%Y-%m-%dT%H:%M:%SZ") |& tee -a $logfile
    bash $home_dir/configureSyslog.sh "$protocol" "$address" "$logfile"
elif [ "$dest" = "file" ];
then
    printf "%s: Configuring filebeat...\n" $(date +"%Y-%m-%dT%H:%M:%SZ") |& tee -a $logfile
    bash $home_dir/configureFilebeat.sh "$format" "$path" "$host_addresses" "$enable_loadbalance" "$logfile" 
fi

printf "%s: Done configuring Server.\n" $(date +"%Y-%m-%dT%H:%M:%SZ") |& tee -a $logfile
