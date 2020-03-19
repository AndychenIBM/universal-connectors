#!/bin/bash

#Load params from configuration file
configfile='configureServer.conf'
protocol=$(grep "protocol" $configfile | cut -d ':' -f 2)
protocol=${protocol^^}
address=$(grep "address" $configfile | cut -d ':' -f2-)
#printf "PROTOCOL=%s\nADDRESS=%s\n\n" "$protocol" "$address"

#Find rsyslog configuration file path
rsyslog_conf=$( readlink  -f /*/rsyslog.conf )
echo "rsyslog configuration file path is : $rsyslog_conf"

#Update rsyslog_conf and restart
sed -i 's/^M//g' $rsyslog_conf


if grep -qF "$address" $rsyslog_conf;then
	echo "address is already located in rsyslog configuration. Please check $rsyslog_conf"
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
	echo "Inserted new line to rsyslog configuration file: $new_line"

	service rsyslog restart
fi
