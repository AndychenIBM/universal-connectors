#!/bin/bash
echo "GI_MODE" $GI_MODE
if [ -z "$GI_MODE" ]
then
    CUSTOMER_PLUGINS_DIR=${LOGSTASH_DIR}/customer/config/
    for file in "${CUSTOMER_PLUGINS_DIR}"/*.rpm; do
      [ -e "$file" ] || continue
      echo "preparing to install ${file##*/}"
      rpm -ivh $CUSTOMER_PLUGINS_DIR/${file##*/} --reinstall
      status=$?
      if [ $status -eq 0 ]; then
        echo "done installing ${file##*/}"
      else
        echo "An error occurred while trying to install a customer dependency."
        exit $status
      fi
    done
else
    CUSTOMER_PLUGINS_DIR=${GI_PLUGINS_DEPENDENCIES_DIR}
    for file in "${CUSTOMER_PLUGINS_DIR}"*/*.rpm; do
      [ -e $file ] || continue
      echo "preparing to install ${file##*/}"
      rpm -ivh $file --reinstall
      status=$?
      if [ $status -eq 0 ]; then
        echo "done installing ${file##*/}"
      else
        echo "An error occurred while trying to install a customer dependency."
        exit $status
      fi
    done
fi
