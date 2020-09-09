#!/bin/bash

CUSTOMER_PLUGINS_DIR=${LOGSTASH_DIR}/customer/config/
for file in "${CUSTOMER_PLUGINS_DIR}"/*.zip; do
  [ -e "$file" ] || continue
  echo "preparing to install ${file##*/}"
  logstash-plugin install file:///$CUSTOMER_PLUGINS_DIR/${file##*/}
  status=$?
  if [ $status -eq 0 ]; then
    echo "done installing ${file##*/}"
    exit 0
  else
    echo "An error occurred while trying to install Logstash plugins."
    exit $status
  fi

done
