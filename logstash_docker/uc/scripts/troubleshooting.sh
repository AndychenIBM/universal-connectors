#!/bin/bash

DEFAULT_ERR_LOG=${LOG_GUC_DIR}/logstash_stdout_stderr.log
ERR_LOG=${1:-$DEFAULT_ERR_LOG}
MAX_ERROR_LOGS_AMOUNT=1000
MAX_ERROR_LOGS_AMOUNT_TO_PRINT=10

# ERROR types:
# 1. errors in plugin installation - UC restores last image (done on the Guardium collector level)
# 2. errors in configuration (syntax, missing credentials, etc...) - conf won't be loaded at all (done on the Guardium collector level)
# 3. wrong credentials, failed connection to cloud env - TODO
# 4. UC is stuck but not down - need to check logs.

# Check if Logstash is up
logstash_pid=$("${UC_SCRIPTS}"/get_logstash_pid.sh)
if [[ -z "$logstash_pid" ]]; then
  echo "logstash is down"
else
  echo "logstash is running"
fi

# Check if ERR_LOG exists
if [[ -f "$ERR_LOG" ]]; then
  echo "$ERR_LOG exists. Checking logs..."
else
  echo "$ERR_LOG doesn't exist."
  exit 0
fi

# Check length
err_cnt=$(grep "ERROR" ${ERR_LOG} | wc -l)
if [ $err_cnt -gt $MAX_ERROR_LOGS_AMOUNT ]; then
   echo "More than ${MAX_ERROR_LOGS_AMOUNT} errors are found on ${ERR_LOG}"
fi

# Connection errors -TODO: extract pattern,err_msg to file
pattern="InvalidURIError: bad URI"
err_msg="InvalidURIError. Please verify credentials correctness and network access"
grep -q "$pattern" $ERR_LOG && echo $err_msg;

pattern="ERROR GuardConnection"
err_msg="Connection to Guardium Sniffer is down. Please verify Sniffer is up"
grep -q "$pattern" $ERR_LOG && echo $err_msg;

pattern="Java::NetSnowflakeClientJdbc::SnowflakeSQLException: Your free trial has ended"
err_msg="Connection to Guardium Sniffer is down. Please verify Sniffer is up"
grep -q "$pattern" $ERR_LOG && echo $err_msg;

pattern="Check your AWS Secret Access Key and signing method."
err_msg="Check your AWS Secret Access Key and signing method."
grep -q "$pattern" $ERR_LOG && echo $err_msg;

pattern="Your free trial has ended"
err_msg="Your free trial has ended. Please make sure your db instance is valid"
grep -q "$pattern" $ERR_LOG && echo $err_msg;

pattern="The specified log group does not exist."
err_msg="The specified log group does not exist."
grep -q "$pattern" $ERR_LOG && echo $err_msg;

pattern="HTTPServerException"
err_msg="AWS domain not added in allowed domain list"
grep -q "$pattern" $ERR_LOG && echo $err_msg;

# TODO - remove duplicates before printing them to logs
# TODO - add more information regarding known exception
#if [ $err_cnt -lt $MAX_ERROR_LOGS_AMOUNT_TO_PRINT ]; then
#   echo $(grep "ERROR" ${ERR_LOG})
#   exit 0
#fi