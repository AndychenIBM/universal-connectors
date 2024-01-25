#!/bin/bash
# **************************************************************
#
# IBM Confidential
#
# OCO Source Materials
#
# 5737-L66
#
# (C) Copyright IBM Corp. 2019, 2024
#
# The source code for this program is not published or otherwise
# divested of its trade secrets, irrespective of what has been
# deposited with the U.S. Copyright Office.
#
# **************************************************************
# send errors to universal connector manager service

# if there is a troubleshooting output then the error is already being handled - no need to add.
output_file="${UC_SCRIPTS}/troubleshooting_output.txt"
if [[ -e "$output_file" && -s "$output_file" && $(cat "$output_file") != "OK" ]]; then
    exit 0
fi

${UC_SCRIPTS}/send_errors_to_kafka.sh