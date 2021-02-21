#!/bin/bash

DEFAULT_UC_HOSTNAME="*.guard.swg.usma.ibm.com"
UC_HOSTNAME=${1:-$DEFAULT_UC_HOSTNAME}

#Create key,certificate in ${SSL_DIR}
openssl req -x509 -batch -nodes -subj "/CN=${UC_HOSTNAME}/" -days 3650 -newkey rsa:2048 -keyout ${SSL_DIR}/app.key -out ${SSL_DIR}/app.crt

#Edit Filebeat ssl input plugin in Logstash configuration:
SSL_CREDENTIALS_STR="ssl => true ssl_certificate => \"$SSL_DIR/app.crt\" ssl_key => \"$SSL_DIR/app.key\""






