#!/bin/bash

DEFAULT_UC_HOSTNAME="*.guard.swg.usma.ibm.com"
UC_HOSTNAME=${1:-$DEFAULT_UC_HOSTNAME}
SSL_DIR=${LOGSTASH_DIR}/ssl
my_openssl_cnf_path=/etc/pki/logstash/my_openssl.cnf

#Create key,certificate in ${SSL_DIR}
rm -r ${SSL_DIR}
mkdir -p ${SSL_DIR}
openssl req -x509 -batch -nodes -subj "/CN=${UC_HOSTNAME}/" -days 3650 -newkey rsa:2048 -keyout ${SSL_DIR}/logstash-beats.key -out ${SSL_DIR}/logstash-beats.crt

#Copy certificate to the shared volume
cp ${SSL_DIR}/logstash-beats.crt /var/log/uc/. #TODO- consider copying to another shared volume

#Edit Filebeat ssl input plugin in Logstash configuration:
SSL_CREDENTIALS_STR="ssl => true ssl_certificate => \"$SSL_DIR/logstash-beats.crt\" ssl_key => \"$SSL_DIR/logstash-beats.key\""
sed  -i "s/beats { port => 5044 type => filebeat }/beats { port => 5044 type => filebeat ${SSL_CREDENTIALS_STR//\//\\/} \}/g" ${LOGSTASH_DIR}/pipeline/mongodb-syslog-filebeat.conf







