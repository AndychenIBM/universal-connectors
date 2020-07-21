#!/bin/bash

for entry in "${LOGSTASH_DIR}/customer/config"/*.gem
do
  ${UC_SCRIPTS}/install_customer_plugin.sh $entry
done