#!/bin/bash

TROUBLESHOOTING_IS_GENERATED_MESSAGE="Troubleshooting script is generated"
UC_LOG=${LOG_GUC_DIR}uc-logstash.log
LOGSTASH_LOG=${LOG_GUC_DIR}logstash-plain.log

LOGS_TO_PROCESS=($UC_LOG $LOGSTASH_LOG )

function returnKnownErrorCodes(){
  ERR_LOG=$1
  ERROR_PATTERN=$2
  LAST_OCCURRENCE_LINE=$3
  pattern=$(echo "$ERROR_PATTERN" | cut -d ";" -f 1)
  err_msg=$(echo "$ERROR_PATTERN" | cut -d ";" -f 2)
  tail -n +$LAST_OCCURRENCE_LINE $ERR_LOG | grep -q "$pattern" && echo "$err_msg
  " ;
}


# Check if Logstash is up
logstash_pid=$("${UC_SCRIPTS}"/get_logstash_pid.sh)
if [[ -z "$logstash_pid" ]]; then
  echo "logstash is down"
fi

function process_log() {
  # Check if ERR_LOG exists
  ERR_LOG=$1
  LAST_OCCURRENCE_LINE=0
  if [[ ! -f "$ERR_LOG" ]]; then
    echo "$ERR_LOG doesn't exist."
    exit 0
  fi

  # Find the last occurrence of TROUBLESHOOTING_IS_GENERATED_MESSAGE in the error logs and keep the line number, set to 0 if not found
  LAST_OCCURRENCE_LINE=$(grep -n -q "$TROUBLESHOOTING_IS_GENERATED_MESSAGE" "$ERR_LOG" && grep -n "$TROUBLESHOOTING_IS_GENERATED_MESSAGE" "$ERR_LOG" | tail -n 1 | cut -d ':' -f 1 || echo 0)
  if [ $LAST_OCCURRENCE_LINE -ne 0 ]; then
    last_occurrence_datetime=$(sed -n "${LAST_OCCURRENCE_LINE}p" "$ERR_LOG" | cut -d' ' -f1,2)
    echo "Checking for errors in the $(basename "$ERR_LOG") file since the last troubleshooting report was generated on $last_occurrence_datetime :"
  else
    echo "Checking for errors in the $(basename "$ERR_LOG") file"
  fi

  # Initialize a flag to keep track of whether any error patterns were found
  found_errors=false

  while IFS= read -r line; do
    if returnKnownErrorCodes "$ERR_LOG" "$line" "$LAST_OCCURRENCE_LINE";then
      found_errors=true
    fi
  done < "${UC_SCRIPTS}/troubleshooting_error_codes.txt"

  # Check the flag after the loop and print the appropriate message
  if ! $found_errors; then
    echo "No errors found in $(basename "$ERR_LOG")"
  fi

  # Log messages with timestamps
  echo "$(date '+%Y-%m-%d %H:%M:%S') - $TROUBLESHOOTING_IS_GENERATED_MESSAGE" >> "$ERR_LOG"
}

# remove sniffer error in case of GI - it's internal and should not be handled by ucm
if [ "$GI_MODE" = "true" ]; then
  sed -i "/ERROR GuardConnection;2;The connection to Guardium Sniffer is down. Please verify Sniffer is up./d" "$UC_SCRIPTS/troubleshooting_error_codes.txt"
fi

for log in ${LOGS_TO_PROCESS[@]}; do
  process_log "$log"
done