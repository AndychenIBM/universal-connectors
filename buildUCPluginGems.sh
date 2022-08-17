BASE_DIR=$(pwd)
function buildUCPluginGem() {
  cd $BASE_DIR
  cd $1
  cp ../../../test/gradle.properties .
  ./gradlew --no-daemon $2 $3 $4
  if [ $? -eq 0 ]; then
    echo "Successfully test $1"
  else
    echo "Failed test $1"
    exit 1
  fi
  ./gradlew --no-daemon gem
  if [ $? -eq 0 ]; then
    echo "Successfully build gem $1"
  else
    echo "Failed build gem $1"
    exit 2
  fi
}

function buildUCCommons() {
  cd universal-connectors-main/common
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
    exit 1
  fi
  cp ./build/libs/common-1.0.0.jar ./build/libs/guardium-universalconnector-commons-1.0.0.jar
  cd ../../
}

buildUCCommons

# Build the rest of the plugins from pluginsToBuild.txt
export UC_ETC=${BASE_DIR}/universal-connectors-main/filter-plugin/logstash-output-guardium/src/resources
grep -v '^#' pluginsToBuild.txt | while read -r line; do buildUCPluginGem "$line" "test"; done

exit 0
