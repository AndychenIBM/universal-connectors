#!/bin/bash
mv ${LOGSTASH_DIR}/customer/pipeline/output_to_guardium.conf ${LOGSTASH_DIR}/customer/pipeline/output_to_guardium.conf_backup
rm -f ${LOGSTASH_DIR}/customer/pipeline/*.conf
mv ${LOGSTASH_DIR}/customer/pipeline/output_to_guardium.conf_backup ${LOGSTASH_DIR}/customer/pipeline/output_to_guardium.conf


