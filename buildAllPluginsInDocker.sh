function testPlugin(){
    cd $1
    cp ../test/gradle.properties .
    ./gradlew --no-daemon $2 $3 $4
    if [ $? -eq 0 ]
    then
      echo "Successfully test $1"
    else
        echo "Failed test $1"
      exit 1
    fi
    ./gradlew --no-daemon gem
    if [ $? -eq 0 ]
    then
      echo "Successfully build gem $1"
    else
    echo "Failed build gem $1"
      exit 2
    fi
    cd ..
}
function buildUcCommons(){
    #build uc-commons
    cd guardium-universalconnector-commons
    ./gradlew test
    if [ $? -eq 0 ]
    then
      echo "Successfully test uc-commons"
    else
        echo "Failed test uc-commons"
      exit 1
    fi
    #check if succeed
    ./gradlew jar
    if [ $? -eq 0 ]
    then
      echo "Successfully build jar uc-commons"
    else
        echo "Failed build jar uc-commons"
      exit 1
    fi
    cd ..
}
buildUcCommons
#(echo && echo "UC_ETC=$PWD/logstash-output-guardium/src/resources") >> ./test/gradle.properties
#test plugins
testPlugin "logstash-filter-mongodb-guardium" "test"
#testPlugin "logstash-filter-mysql-guardium"
testPlugin "logstash-filter-s3-guardium" "test"
testPlugin "logstash-filter-mysql-percona-guardium" "test"
#testPlugin "logstash-input-cloudwatch-logs-master" "test"
export UC_ETC=$PWD/logstash-output-guardium/src/resources

#./logstash-output-guardium/gradlew setUTC -PUC_ETC=$PWD/logstash-output-guardium/src/resources
#testPlugin "logstash-output-guardium" "setUTC" "-PUC_ETC=$PWD/logstash-output-guardium/src/resources"
testPlugin "logstash-output-guardium" "test"
#return success code
exit 0