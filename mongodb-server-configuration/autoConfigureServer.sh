echo "Configuring MongoDB auditLog..."
bash configureMongodb.sh
echo "Configuring syslog..."
bash configureSyslog.sh
echo "Done configuring Server."