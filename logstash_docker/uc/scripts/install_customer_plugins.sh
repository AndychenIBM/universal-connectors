#!/bin/bash

CUSTOMER_PLUGINS_DIR=${LOGSTASH_DIR}/customer/config/
for file in "${CUSTOMER_PLUGINS_DIR}"/*.zip; do
  [ -e "$file" ] || continue
  echo "preparing to install ${file##*/}"
  logstash-plugin install file:///$CUSTOMER_PLUGINS_DIR/${file##*/}
  echo "done intalling ${file##*/}"
done
