#!/bin/bash

TROUBLESHOOTING_IS_GENERATED_MESSAGE="Troubleshooting script is generated"
LOGSTASH_FAILED=${2:-false}
UC_LOG=${LOG_GUC_DIR}uc-logstash.log
LOGSTASH_LOG=${LOG_GUC_DIR}logstash-plain.log
# for logging
TROUBLESHOOTING_LOG=${LOG_GUC_DIR}troubleshooting.log

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

# This function search for errors that are not in the list of known error codes in case of fatal issue.
function returnUnknownError(){
  ERR_LOG=$1
  LAST_OCCURRENCE_LINE=$2

  fatal_func="handleFatalError"
  log_level_error='{"loglevel":"error"'
  error_prefix=":exception=>"
  error_suffix=", :backtrace"
  # if there is fatal error in logstash, then
  if grep -q $fatal_func "$ERR_LOG"; then
    # find all the logs of "error" level and search for the exception message. (from ':exception=>' to ', :backtrace') for example: {"loglevel":"error","host":"sysqaqu5v8yjzquscvqld7y7bhc33-universal-connector-0","message":"Pipeline error {:pipeline_id=>"customer_pipeline", :exception=>#<LogStash::ConfigurationError: Can't create a connection pool to the database>, :backtrace=>["/usr/share/logstash/vendor/bundle/jruby/3.1.0/gems/logstash-integration-jdbc-5.4.4/lib/logstash/inputs/jdbc.rb:318:in `register'", "/usr/share/logstash/vendor/bundle/jruby/3.1.0/gems/logstash-mixin-ecs_compatibility_support-1.3.0-java/lib/logstash/plugin_mixins/ecs_compatibility_support/target_check.rb:48:in `register'", "/usr/share/logstash/logstash-core/lib/logstash/java_pipeline.rb:237:in `block in register_plugins'", "org/jruby/RubyArray.java:1987:in `each'", "/usr/share/logstash/logstash-core/lib/logstash/java_pipeline.rb:236:in `register_plugins'", "/usr/share/logstash/logstash-core/lib/logstash/java_pipeline.rb:395:in `start_inputs'", "/usr/share/logstash/logstash-core/lib/logstash/java_pipeline.rb:320:in `start_workers'", "/usr/share/logstash/logstash-core/lib/logstash/java_pipeline.rb:194:in `run'", "/usr/share/logstash/logstash-core/lib/logstash/java_pipeline.rb:146:in `block in start'"], "pipeline.sources"=>["/usr/share/logstash/config/pipeline/Guardium_SAPHANA_filter_34.conf", "/usr/share/logstash/config/pipeline/JDBC_input_33.conf", "/usr/share/logstash/config/pipeline/heartbeat_input.conf", "/usr/share/logstash/config/pipeline/output_guardium.conf", "/usr/share/logstash/config/pipeline/zzz_metrics_filter.conf"], :thread=>"#<Thread:0xee2e690 /usr/share/logstash/logstash-core/lib/logstash/java_pipeline.rb:134 run>"}","timestamp":"2023-10-17T09:33:32+0000", "file":"logstash.javapipeline(LoggerExt.java:127)","service_name":"logstash.javapipeline","x-correlation-id": "","tenant_id": "TNT_QU5V8YJZQUSCVQLD7Y7BHC","func":"rubyError","product_title": ""}
    root_cause=$(tail -n +$LAST_OCCURRENCE_LINE "$ERR_LOG" | grep "$log_level_error" | \
    awk -v error_prefix="$error_prefix" -v error_suffix="$error_suffix" \
    'match($0, error_prefix "(.*?)" error_suffix) {print substr($0, RSTART + length(error_prefix), RLENGTH - length(error_prefix) - length(error_suffix))}')
    if [ -n "$root_cause" ];then
      echo "$root_cause"
    else
      # a general message in case of fatal error of logstash and no root cause found.
      echo "There is an unexpected issue with the universal connector. Contact IBM Support."
    fi
  fi
}


# Check if Logstash is up
logstash_pid=$("${UC_SCRIPTS}"/get_logstash_pid.sh)
if [[ $LOGSTASH_FAILED = "false"  && -z "$logstash_pid" ]]; then
  echo "logstash is down" >> $TROUBLESHOOTING_LOG
fi

function process_log() {
  # Check if ERR_LOG exists
  ERR_LOG=$1
  LAST_OCCURRENCE_LINE=0
  if [[ ! -f "$ERR_LOG" ]]; then
    echo "$ERR_LOG doesn't exist." >> $TROUBLESHOOTING_LOG
    exit 0
  fi

  # Find the last occurrence of TROUBLESHOOTING_IS_GENERATED_MESSAGE in the error logs and keep the line number, set to 0 if not found
  LAST_OCCURRENCE_LINE=$(grep -n -q "$TROUBLESHOOTING_IS_GENERATED_MESSAGE" "$ERR_LOG" && grep -n "$TROUBLESHOOTING_IS_GENERATED_MESSAGE" "$ERR_LOG" | tail -n 1 | cut -d ':' -f 1 || echo 0)
  if [ $LAST_OCCURRENCE_LINE -ne 0 ]; then
    last_occurrence_datetime=$(sed -n "${LAST_OCCURRENCE_LINE}p" "$ERR_LOG" | cut -d' ' -f1,2)
    echo "Checking for errors in the $(basename "$ERR_LOG") file since the last troubleshooting report was generated on $last_occurrence_datetime :" >> $TROUBLESHOOTING_LOG
  else
    echo "Checking for errors in the $(basename "$ERR_LOG") file" >> $TROUBLESHOOTING_LOG
  fi

  # Initialize a flag to keep track of whether any error patterns were found
  found_errors=false

  while IFS= read -r line; do
    if returnKnownErrorCodes "$ERR_LOG" "$line" "$LAST_OCCURRENCE_LINE";then
      found_errors=true
    fi
  done < "${UC_SCRIPTS}/troubleshooting_error_codes.txt"

  # if there aren't any known error codes, check if there is a fatal error and search for the root cause.
  if  ! $found_errors; then
    fatal_error=$(returnUnknownError "$ERR_LOG" "$LAST_OCCURRENCE_LINE")
    if [ -n "$fatal_error" ];then
      found_errors=true
      echo "$fatal_error"
    fi
  fi

  # Check the flag after the loop and print the appropriate message
  if ! $found_errors; then
    # PAY ATTENTION: other code parts expect the output to be "OK".
    # DO NOT change this value!
    echo "OK"

    # for logging
    echo "No errors found in $(basename "$ERR_LOG")" >> $TROUBLESHOOTING_LOG
  fi

  # Log messages with timestamps
  echo "$(date '+%Y-%m-%d %H:%M:%S') - $TROUBLESHOOTING_IS_GENERATED_MESSAGE" >> "$ERR_LOG"
}

# remove sniffer error in case of GI - it's internal and should not be handled by ucm
if [ "$GI_MODE" = "true" ]; then
  sed -i "/ERROR GuardConnection;2;The connection to Guardium Sniffer is down. Please verify Sniffer is up./d" "$UC_SCRIPTS/troubleshooting_error_codes.txt"
fi


found_errors=false
for log in ${LOGS_TO_PROCESS[@]}; do
  result=$(process_log "$log")
  if [[  -n "$result" && "$result" != "OK" ]]; then
    echo "$result"
    found_errors=true
  fi
done

if ! $found_errors; then
  # PAY ATTENTION: other code parts expect the output to be "OK".
  # DO NOT change this value!
  echo "OK"
fi
