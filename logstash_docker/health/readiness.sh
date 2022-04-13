#!/bin/bash
# **************************************************************
#
# IBM Confidential
#
# OCO Source Materials
#
# 5737-L66
#
# (C) Copyright IBM Corp. 2019, 2022
#
# The source code for this program is not published or otherwise
# divested of its trade secrets, irrespective of what has been
# deposited with the U.S. Copyright Office.
#
# **************************************************************

head -n 1 /proc/1/sched | grep java
if [ $? -eq 0 ]; then
  exit 0
else
  exit 1
fi
