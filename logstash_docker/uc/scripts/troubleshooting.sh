#!/bin/bash

TROUBLESHOOTING_IS_GENERATED_MESSAGE="Troubleshooting script is generated"
DEFAULT_ERR_LOG=${LOG_GUC_DIR}logstash_stdout_stderr.log
ERR_LOG=${1:-$DEFAULT_ERR_LOG}
LOGSTASH_FAILED=${2:-false}
MAX_ERROR_LOGS_AMOUNT=1000
LAST_OCCURRENCE_LINE=0


function returnKnownErrorCodes(){
  pattern=$(echo "$1" | cut -d ";" -f 1)
  err_code=$(echo "$1" | cut -d ";" -f 2)
  err_msg=$(echo "$1" | cut -d ";" -f 3)
  tail -n +$LAST_OCCURRENCE_LINE $ERR_LOG | grep -q "$pattern" && echo "$err_msg" ;
}


# Check if Logstash is up
logstash_pid=$("${UC_SCRIPTS}"/get_logstash_pid.sh)
if [[ $LOGSTASH_FAILED = "false"  && -z "$logstash_pid" ]]; then
  echo "logstash is down"
fi


# Check if ERR_LOG exists
if [[ ! -f "$ERR_LOG" ]]; then
  echo "$ERR_LOG doesn't exist."
  exit 0
fi


# Check length
err_cnt=$(grep "ERROR" ${ERR_LOG} | wc -l)
if [ $err_cnt -gt $MAX_ERROR_LOGS_AMOUNT ]; then
   echo "More than ${MAX_ERROR_LOGS_AMOUNT} errors are found on ${ERR_LOG}"
fi


# Find the last occurrence of TROUBLESHOOTING_IS_GENERATED_MESSAGE in the error logs and keep the line number, set to 0 if not found
LAST_OCCURRENCE_LINE=$(grep -n -q "$TROUBLESHOOTING_IS_GENERATED_MESSAGE" "$ERR_LOG" && grep -n "$TROUBLESHOOTING_IS_GENERATED_MESSAGE" "$ERR_LOG" | tail -n 1 | cut -d ':' -f 1 || echo 0)
if [ $LAST_OCCURRENCE_LINE -ne 0 ]; then
  last_occurrence_datetime=$(sed -n "${LAST_OCCURRENCE_LINE}p" "$ERR_LOG" | cut -d' ' -f1,2)
  echo "Checking for errors since the last troubleshooting report was generated on $last_occurrence_datetime :"

fi

# remove sniffer error in case of GI - it's internal and should not be handled by ucm
if [ "$GI_MODE" = "true" ]; then
  sed -i "/ERROR GuardConnection;2;The connection to Guardium Sniffer is down. Please verify Sniffer is up./d" "$UC_SCRIPTS/troubleshooting_error_codes.txt"
fi

# Connection errors

# Initialize a flag to keep track of whether any error patterns were found
found_errors=false

while IFS= read -r line; do
  if returnKnownErrorCodes "$line"; then
    found_errors=true
  fi
done < "${UC_SCRIPTS}/troubleshooting_error_codes.txt"

# Check the flag after the loop and print the appropriate message
if ! $found_errors; then
  echo "OK"
fi

# Log messages with timestamps
echo "$(date '+%Y-%m-%d %H:%M:%S') - $TROUBLESHOOTING_IS_GENERATED_MESSAGE" >> "$DEFAULT_ERR_LOG"

exit 0
