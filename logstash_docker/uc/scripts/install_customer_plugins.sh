#!/bin/bash

if [ -z "$GI_MODE" ]
then
    CUSTOMER_PLUGINS_DIR=${LOGSTASH_DIR}/customer/config/
else
    CUSTOMER_PLUGINS_DIR=${GI_PLUGINS_DIR}
fi

for file in "${CUSTOMER_PLUGINS_DIR}"/*.zip; do
  [ -e "$file" ] || continue
  echo "preparing to install ${file##*/}"
  logstash-plugin install file:///$CUSTOMER_PLUGINS_DIR/${file##*/}
  status=$?
  if [ $status -eq 0 ]; then
    echo "done installing ${file##*/}"
  else
    echo "An error occurred while trying to install Logstash plugins."
    exit $status
  fi

done
