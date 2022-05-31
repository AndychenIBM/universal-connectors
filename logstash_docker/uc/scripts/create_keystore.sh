#!/bin/bash

# **************************************************************
#
# IBM Confidential
#
# OCO Source Materials
#
# 5737-L66
#
# (C) Copyright IBM Corp. 2019, 2022
#
# The source code for this program is not published or otherwise
# divested of its trade secrets, irrespective of what has been
# deposited with the U.S. Copyright Office.
#
# **************************************************************

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
INSIGHT_KEYSTORE="${SSL_DIR}/insights.jks"
# ROOT_CA_LOCATION="/etc/pki/tls/certs/rootCA.crt"
ROOT_CA_LOCATION="/etc/pki/tls/certs/insights-rootca.crt"

echo "start keystore creation"

# verify CA exists
if [ ! -f  ${ROOT_CA_LOCATION} ]; then
    echo "Could not create key store for root CA cause ${ROOT_CA_LOCATION} not exists"
    exit 1
fi

# keystore password
if [ "" == "$UNIVERSAL_CONNECTOR_KEYSTORE_PASSWORD" ]; then
    UNIVERSAL_CONNECTOR_KEYSTORE_PASSWORD="U2FsdGVkX1+bxFHfpfCRkeIQzwwggZJikTmZZkwZkLE="
fi

GCMSCRIPTDIR="$BASEDIR/../utils"

if [ "$ENCRYPTION_ALG" == "GCM"  ]; then
    echo "Using GCM decryption"
    keystore_pwd=$(java -cp $GCMSCRIPTDIR GCMDecrypt $UNIVERSAL_CONNECTOR_KEYSTORE_PASSWORD)
    echo "Finished GCM decryption"
else
    echo "Using CBC decryption"
    keystore_pwd=$(echo -n $UNIVERSAL_CONNECTOR_KEYSTORE_PASSWORD | openssl enc -aes-256-cbc -d -base64 -salt -pbkdf2 -A -k $MASTER_KEY)
    echo "Finished CBC decryption"
fi

# remove existing KeyStore on startup just in case certificates were updated
if [ -f ${INSIGHT_KEYSTORE} ]; then
    rm -f ${INSIGHT_KEYSTORE} || echo "ERROR: Could not delete insights.jks"
fi

# create a keystore specifically to communicate from logstash-output-guardium plugin to mini sniff
if [ ! -f ${INSIGHT_KEYSTORE} ]; then
    # create keystore
    echo "Creating keystore: ${INSIGHT_KEYSTORE}"
    keytool -genkey -noprompt \
    -alias "IBM Security Guardium Insights" \
    -dname "CN=insights.guardium.security.ibm.com, OU=Security, O=IBM, L=Armonk, S=NY, C=US" \
    -keystore "${INSIGHT_KEYSTORE}" \
    -storetype PKCS12 \
    -keyalg RSA \
    -keysize 2048 \
    -storepass "$keystore_pwd" \
    -keypass "$keystore_pwd"

    # verify keystore is created
    if [ ! -f  ${INSIGHT_KEYSTORE} ]; then
        echo "Could not find ${INSIGHT_KEYSTORE}"
        exit 1
    fi

    # add root CA to keystore
    echo "Adding root CA to keystore"
    keytool -import -v -trustcacerts -alias "rootCA" -file "${ROOT_CA_LOCATION}" \
        -keystore "${INSIGHT_KEYSTORE}" -storepass "${keystore_pwd}" -noprompt
fi