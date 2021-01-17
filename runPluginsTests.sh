cd logstash-uc-commons
./gradlew jar
cd ../logstash-output-guardium
export UC_ETC=$(pwd)/src/resources
./gradlew test