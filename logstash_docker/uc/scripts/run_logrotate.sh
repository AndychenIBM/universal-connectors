#!/bin/bash

#echo "Going to run chrond"
crond -P

echo "Going to create /etc/logrotate.d/uc-logrotate-config file"
FILE="/etc/logrotate.d/uc-logrotate-config"
echo "/var/log/uc/logstash_stdout_stderr.log {
        su guc guc
        size 100k
        rotate 10
        compress
        copytruncate
        nocreate
        nodateext
      }" > $FILE

echo "Going to change permissions on logrotate conf"
chmod 0644 /etc/logrotate.d/uc-logrotate-config

echo "Going to add cron job for logrotate"
(crontab -l 2>/dev/null || true; echo "*/5 * * * * logrotate /etc/logrotate.conf") | crontab -

