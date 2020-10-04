#!/bin/bash

DEFAULT_UC_HOSTNAME="*.guard.swg.usma.ibm.com"
UC_HOSTNAME=${1:-$DEFAULT_UC_HOSTNAME}

#Create key,certificate in ${SSL_DIR}
openssl req -x509 -batch -nodes -subj "/CN=${UC_HOSTNAME}/" -days 3650 -newkey rsa:2048 -keyout ${UC_EXTERNAL_CONFIG}/logstash-beats.key -out ${UC_EXTERNAL_CONFIG}/logstash-beats.crt

#Edit Filebeat ssl input plugin in Logstash configuration:
SSL_CREDENTIALS_STR="ssl => true ssl_certificate => \"$UC_EXTERNAL_CONFIG/logstash-beats.crt\" ssl_key => \"$UC_EXTERNAL_CONFIG/logstash-beats.key\""

#sed  -i "s/beats { port => 5044 type => filebeat }/beats { port => 5044 type => filebeat ${SSL_CREDENTIALS_STR//\//\\/} \}/g" ${LOGSTASH_DIR}/pipeline/mongodb-syslog-filebeat.conf







