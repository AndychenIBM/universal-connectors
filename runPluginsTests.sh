find / -name "jdk"
echo $JAVA_HOME
cd logstash-uc-commons
chmod 755 ./gradlew
./gradlew jar
cd ../logstash-output-guardium
export UC_ETC=$(pwd)/src/resources
chmod 755 ./gradlew
./gradlew test