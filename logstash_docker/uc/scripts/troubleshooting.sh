#!/bin/bash

DEFAULT_ERR_LOG=${LOG_GUC_DIR}/logstash_stdout_stderr.log
ERR_LOG=${1:-DEFAULT_ERR_LOG}
MAX_ERROR_LOGS_AMOUNT=500
MAX_ERROR_LOGS_AMOUNT_TO_PRINT=10

# ERROR types:
# 1. errors in plugin installation - UC restores last image (done on the Guardium collector level)
# 2. errors in configuration (syntax, missing credentials, etc...) - conf won't be loaded at all (done on the Guardium collector level)
# 3. wrong credentials, failed connection to cloud env - TODO
# 4. UC is stuck but not down - need to check logs.

# Check if Logstash is up
logstash_pid=$("${UC_SCRIPTS}"/get_logstash_pid.sh)
if [[ -z "$logstash_pid" ]]; then
  echo "logstash is running"
else
  echo "logstash is down"
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
   echo "More than ${MAX_ERROR_LOGS_AMOUNT} are found on ${ERR_LOG}"
   exit 0
fi

# TODO - remove duplicates before printing them to logs
# TODO - add more information regarding known exception
if [ $err_cnt -lt $MAX_ERROR_LOGS_AMOUNT_TO_PRINT ]; then
   echo $(grep "ERROR" ${ERR_LOG})
   exit 0
fi