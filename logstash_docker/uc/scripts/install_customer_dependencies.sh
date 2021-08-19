#!/bin/bash

if [ -z "$GI_MODE" ]
then
    CUSTOMER_PLUGINS_DIR=${LOGSTASH_DIR}/customer/config/
else
    CUSTOMER_PLUGINS_DIR=${GI_PLUGINS_DIR}
fi

for file in "${CUSTOMER_PLUGINS_DIR}"/*.rpm; do
  [ -e "$file" ] || continue
  echo "preparing to install ${file##*/}"
  rpm -ivh $CUSTOMER_PLUGINS_DIR/${file##*/} --reinstall
  status=$?
  if [ $status -eq 0 ]; then
    echo "done installing ${file##*/}"
    exit 0
  else
    echo "An error occurred while trying to install a customer dependency."
    exit $status
  fi

done
