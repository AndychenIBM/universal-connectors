#!/bin/bash

DEFAULT_ERR_LOG=${LOG_GUC_DIR}logstash_stdout_stderr.log
ERR_LOG=${1:-$DEFAULT_ERR_LOG}
MAX_ERROR_LOGS_AMOUNT=1000

function returnKnownErrorCodes(){
  pattern=$(echo "$1" | cut -d ";" -f 1)
  err_code=$(echo "$1" | cut -d ";" -f 2)
  err_msg=$(echo "$1" | cut -d ";" -f 3)

  grep -q "$pattern" $ERR_LOG && echo "message: $err_msg" && exit $err_code;
}

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

# Connection errors
grep -v '^#' ${UC_SCRIPTS}/troubleshooting_error_codes.txt | while read -r line ; do returnKnownErrorCodes "$line"; done
exit 0