#!/bin/bash

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

#Building Java GCM decryption file
javac $BASEDIR/logstash_docker/uc/utils/GCMDecrypt.java 

exit 0