#!/bin/bash
BASE_DIR=$(pwd)

function adjustToLogstash8() {
  sed -i 's/logstash-core-*.*.*.jar/logstash-core.jar/' build.gradle
  sed -i '/ext { snakeYamlVersion.*/d' build.gradle
  sed -i '/^buildscript.*/a ext { snakeYamlVersion = '$snakeYamlVersion' }' build.gradle

}
function buildUCPluginGem() {
  echo "================ Building $1 gem file================"
  cd ${BASE_DIR}/${UC_OPENSOURCE_ROOT_DIR}/$1
  adjustToLogstash8
  cp ../../../test/gradle.properties .
  ./gradlew --no-daemon $2 $3 $4 </dev/null >/dev/null 2>&1
  if [ $? -eq 0 ]; then
    echo "Successfully test $1"
  else
    echo "Failed test $1"
  fi
  ./gradlew --no-daemon gem </dev/null >/dev/null 2>&1
  if [ $? -eq 0 ]; then
    echo "Successfully build gem $1"
  else
    echo "Failed build gem $1"
  fi
}

function buildUCCommons() {
  cd ${UC_OPENSOURCE_ROOT_DIR}/common
  ./gradlew test >/dev/null 2>&1
  if [ $? -eq 0 ]; then
    echo "Successfully test uc-commons"
  else
    echo "Failed test uc-commons"
    exit 1
  fi
  #check if succeed
  ./gradlew jar >/dev/null 2>&1
  if [ $? -eq 0 ]; then
    echo "Successfully build jar uc-commons"
  else
    echo "Failed build jar uc-commons"
    exit 2
  fi
  cp ./build/libs/common-1.0.0.jar ./build/libs/guardium-universalconnector-commons-1.0.0.jar
  cd ../../
}

function buildRubyPlugin(){
  cd ${BASE_DIR}/$1
  bundle install >/dev/null 2>&1
  gem build $2
}

buildUCCommons

snakeYamlVersion=$(grep -E "snakeYamlVersion =" /usr/share/logstashSRC/logstash/build.gradle | cut -d \' -f 2)
echo "snakeYamlVersion is $snakeYamlVersion"

# Build the rest of the plugins from pluginsToBuild.txt
export UC_ETC=${BASE_DIR}/${UC_OPENSOURCE_ROOT_DIR}/filter-plugin/logstash-output-guardium/src/resources
grep -v '^#' pluginsToBuild.txt | while read -r line; do buildUCPluginGem "$line" "test";done
grep -v '^#' pluginsToBuildNotFromOpenSource.txt | while read -r line; do buildUCPluginGem "$line" "test"; done

buildRubyPlugin logstash-input-cloudwatch-logs-master logstash-input-cloudwatch_logs.gemspec
#grep -v '^#' ${BASE_DIR}/rubyPluginsToBuild.txt | while read -r line; do buildRubyPlugin "${UC_OPENSOURCE_ROOT_DIR}/${line}" "${line##*/}.gemspec"; done

exit 0
