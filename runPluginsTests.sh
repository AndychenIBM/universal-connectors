echo AAA
pwd
echo BBB
cd logstash-uc-commons
chmod 755 ./gradlew
./gradlew jar
cd ../logstash-output-guardium
rm -rf logstash
mkdir logstash
cd logstash
git clone https://github.com/elastic/logstash.git
cd ..
export UC_ETC=$(pwd)/src/resources
cp ./gradle.propertiesTravis ./gradle.properties
chmod 755 ./gradlew
./gradlew test