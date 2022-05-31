#!/bin/bash

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

#Building Java GCM decryption file
javac $BASEDIR/logstash_docker/uc/utils/GCMDecrypt.java 

exit 0
#build uc-commons
cd logstash-uc-commons
chmod 755 ./gradlew
./gradlew jar
#build outputplugin
cd ../logstash-output-guardium
rm -rf logstash
mkdir logstash
cd logstash
#install ruby and clone logstash
sudo apt-get install ruby-full
echo $LOGSTASH_VERSION_TAG
git clone -b "$LOGSTASH_VERSION_TAG" https://github.com/elastic/logstash.git
cd logstash
chmod 755 ./gradlew
./gradlew -q :logstash-core:jar
