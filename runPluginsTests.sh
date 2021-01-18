echo AAA
pwd
cat /etc/os-release
echo BBB
cd logstash-uc-commons
chmod 755 ./gradlew
./gradlew jar
cd ../logstash-output-guardium
rm -rf logstash
mkdir logstash
cd logstash
sudo apt-get install ruby-full
git clone https://github.com/elastic/logstash.git
cd ..
export UC_ETC=$(pwd)/src/resources
cp ./gradle.propertiesTravis ./gradle.properties
chmod 755 ./gradlew
./gradlew test