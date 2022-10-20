#!/bin/bash
BASE_DIR=$(pwd)
function buildUCPluginGem() {
  echo "================ Building $1 gem file================"
  cd $BASE_DIR/${UC_OPENSOURCE_ROOT_DIR}/$1
  cp ../../../test/gradle.properties .
  ./gradlew --no-daemon $2 $3 $4 </dev/null
  if [ $? -eq 0 ]; then
    echo "Successfully test $1"
  else
    echo "Failed test $1"
  fi
  ./gradlew --no-daemon gem </dev/null
  if [ $? -eq 0 ]; then
    echo "Successfully build gem $1"
  else
    echo "Failed build gem $1"
  fi
}

function buildUCCommons() {
  cd ${UC_OPENSOURCE_ROOT_DIR}/common
  ./gradlew test
  if [ $? -eq 0 ]; then
    echo "Successfully test uc-commons"
  else
    echo "Failed test uc-commons"
    exit 1
  fi
  #check if succeed
  ./gradlew jar
  if [ $? -eq 0 ]; then
    echo "Successfully build jar uc-commons"
  else
    echo "Failed build jar uc-commons"
    exit 2
  fi
  cp ./build/libs/common-1.0.0.jar ./build/libs/guardium-universalconnector-commons-1.0.0.jar
  cd ../../
}

buildUCCommons

# Build the rest of the plugins from pluginsToBuild.txt
export UC_ETC=${BASE_DIR}/${UC_OPENSOURCE_ROOT_DIR}/filter-plugin/logstash-output-guardium/src/resources
grep -v '^#' pluginsToBuild.txt | while read -r line; do buildUCPluginGem "$line" "test";done
grep -v '^#' pluginsToBuildNotFromOpenSource.txt | while read -r line; do buildUCPluginGem "$line" "test"; done
exit 0
