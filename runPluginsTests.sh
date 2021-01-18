cd logstash-uc-commons
chmod 755 ./gradlew
./gradlew jar
cd ../logstash-output-guardium
export UC_ETC=$(pwd)/src/resources
cp ./gradle.propertiesTravis ./gradle.properties
chmod 755 ./gradlew
./gradlew test