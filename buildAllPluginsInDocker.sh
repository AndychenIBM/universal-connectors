function testPlugin(){
    cd $1
    cp ../test/gradle.properties .
    ./gradlew $2 $3 $4
    if [ $? -eq 0 ]
    then
      echo "Successfully test $1"
    else
        echo "Failed test $1"
      exit 1
    fi
    ./gradlew gem
    if [ $? -eq 0 ]
    then
      echo "Successfully build gem $1"
    else
    echo "Failed build gem $1"
      exit 2
    fi
    cd ..
}

#build uc-commons
cd logstash-uc-commons
./gradlew test
#check if succeed
./gradlew jar
cd ..
(echo && echo "UC_ETC=$PWD/logstash-output-guardium/src/resources") >> ./test/gradle.properties
#test plugins
testPlugin "logstash-filter-mongodb-guardium" "test"
#testPlugin "logstash-filter-mysql-guardium"
testPlugin "logstash-filter-s3-guardium" "test"
#testPlugin "logstash-input-cloudwatch-logs-master" "test"
export UC_ETC=$PWD/logstash-output-guardium/src/resources

#./logstash-output-guardium/gradlew setUTC -PUC_ETC=$PWD/logstash-output-guardium/src/resources
testPlugin "logstash-output-guardium" "setUTC" "-PUC_ETC=$PWD/logstash-output-guardium/src/resources"

#return success code
exit 0