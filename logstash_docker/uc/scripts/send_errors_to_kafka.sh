#!/bin/bash
# **************************************************************
#
# IBM Confidential
#
# OCO Source Materials
#
# 5737-L66
#
# (C) Copyright IBM Corp. 2019, 2023
#
# The source code for this program is not published or otherwise
# divested of its trade secrets, irrespective of what has been
# deposited with the U.S. Copyright Office.
#
# **************************************************************

# This script runs search for errors in the logs and if it finds an unrecoverable error it sends it to uc manager via kafka.
# In case that logstash crashed it only prepares the new configuration pipeline and *does not* starting logstash again - starting logstash again is out of this scope.

echo "start send_errors_to_kafka"
logstash_pid=$(${UC_SCRIPTS}/get_logstash_pid.sh)

IS_FAILED=${1:-false}

# output file location from env
if [ -n "$TROUBLESHOOT_OUTPUT" ]; then
    output_file="$TROUBLESHOOT_OUTPUT"
else
    output_file="${UC_SCRIPTS}/troubleshooting_output.txt"
fi

# check that uc log exists
if [[ ! -f "${LOG_GUC_DIR}uc-logstash.log" ]]; then
  echo "${LOG_GUC_DIR}uc-logstash.log doesn't exist."
  exit 0
fi
# run troubleshooting on uc-logstash.log
${UC_SCRIPTS}/troubleshooting.sh ${LOG_GUC_DIR}uc-logstash.log $IS_FAILED > $output_file

result=$(cat $output_file)
echo "TROUBLESHOOTING script result: $result"

# if there are errors then remove original configuration, replace it with new configuration for sending errors to ucm
if [[  -n "$result" && "$result" != "OK" ]]; then
  echo "Universal connector ${INPUT_PLUGIN_ID} of tenant ${TENANT_ID} failed because of an error: $result"
  rm -f ${UC_ETC}/pipeline/*.conf
  # copy the result because the file is gonna be deleted in the end of the pipeline
  echo "$result" > "${LOG_GUC_DIR}troubleshooting_output.txt"
  mv "${LOGSTASH_DIR}/pipeline/troubleshooting.conf" "${UC_ETC}/pipeline"
  echo "Starting new logstash pipeline for sending the error to kafka topic: 'data_flow'"
  if [[ $IS_FAILED = "false" ]]; then
    ${UC_SCRIPTS}/reload_logstash_conf.sh
  fi
fi

exit 0