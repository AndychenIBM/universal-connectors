#!/bin/bash
#Author:Ora Shapiro

#Load params from configuration file
configfile='configureServer.conf'
dest=$(grep "destination" $configfile | cut -d ':' -f 2)
format=$(grep "format" $configfile | cut -d ':' -f 2)
path=$(grep "path" $configfile | cut -d ':' -f 2)
filter=$(grep "filter" $configfile | cut -d ':' -f2-)
protocol=$(grep "protocol" $configfile | cut -d ':' -f 2)
protocol=${protocol^^}
address=$(grep "address" $configfile | cut -d ':' -f2-)
#printf "DEST=%s\nFORMAT=%s\nPATH=%s\nFILTER=%s\nADDRESS=%s\nPROTOCOL=%s\n\n" "$dest" "$format" "$path" "$filter" "$address" "$protocol" 

#Find mongod status
service mongod status | tr '\t' ',' > mon_status.txt
cat mon_status.txt
sed -i 's/mongod (pid //g' mon_status.txt
sed -i 's/) is running...//g' mon_status.txt
sed -i 's/\s*$//g' mon_status.txt
 
#Find mongod pid if exists
mon_pid=$( cat mon_status.txt )
echo " Mongodb  pid/status $mon_pid"
ps -ef | grep $mon_pid | tr '\t' ',' > monconf_path.txt
 
#Find mongod configuration file path
mongod_conf=$( readlink  -f /*/mongod.conf )
echo "Mongodb configuration file path is : $mongod_conf"
 
#Find mongod pid file path configuration
sed -n /'mongod.pid'/p $mongod_conf > mon_pid_path.txt
sed -i 's/ pidFilePath: //g' mon_pid_path.txt
sed -i 's/ # location of pidfile//g' mon_pid_path.txt
sed -i 's/\s*$//g' mon_status.txt
mongo_pid_path=$(cat mon_pid_path.txt)
echo "Mongodb pid file path is : $mongo_pid_path"
#read -p "Press [Enter] key to continue..."	
 
#Find if audit section exists and set in mongod.conf
#If audit section exists , update audit destination to syslog and update filters, if it does not exist, add the audit section to mongod.conf"
if grep -lq  '^auditLog' $mongod_conf
	then
		echo "AuditLog will be set to syslog in mongod.conf"	
		#startRange=$(awk '/auditLog/{ print NR; exit }' $mongod_conf)
		#echo $startRange
		#endRange=$((startRange+10))
		#echo $endRange
		sed -i "s/   destination:.*/   destination: $dest/g" $mongod_conf
		sed -i "s/   format:.*/   format: $format/g" $mongod_conf
		sed -i "s/   path:.*/   path: $path/g" $mongod_conf
		#sed -i "s/   #filter:.*/   filter: $filter/g" $mongod_conf
		#sed -i "s/   ^filter:.*/   filter: $filter/g" $mongod_conf
		sed -i "s/#setParameter: {auditAuthorizationSuccess: true}/setParameter: {auditAuthorizationSuccess: true}/g" $mongod_conf
	else
		echo "AuditLog section will be added to mongod.conf"
		printf "\nauditLog:\n$dest\n$filter\n""setParameter: {auditAuthorizationSuccess: true}  ">> $mongod_conf
fi	
 
#Restart mongod service after the configuration changes
service mongod stop
rm -f $mongo_pid_path
rm -f /tmp/mongodb-27017.sock
sleep 2
service mongod start
rm -f mon_status.txt mon_pid_path.txt monconf_path.txt
#sleep 6
#mongod --auth --port 27017

#Find rsyslog configuration file path
rsyslog_conf=$( readlink  -f /*/rsyslog.conf )
echo "rsyslog configuration file path is : $rsyslog_conf"

#Insert new address
if grep "9.70.145.96:5141" $rsyslog_conf
then
	echo "address is already located in rsyslog configuration. Please check $rsyslog_conf"
else
	last_programname_appearance=$(cat -n $rsyslog_conf | grep "programname" | tail -1 |  cut -f 1)
	last_programname_appearance=$((last_programname_appearance+1))
	last_programname_appearance+="i"
	echo "last program name appearance: $last_programname_appearance"
	new_line=":programname, isequal, \"mongod\" @$address"
	echo $new_line  
	sed -i "$last_programname_appearance$new_line" $rsyslog_conf
	service rsyslog restart
fi

#tcp- starts with "@@", udp- starts with "@"

