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
git clone -b 7.5 https://github.com/elastic/logstash.git
cd logstash
chmod 755 ./gradlew
./gradlew -q :logstash-core:build
cd ../../
export UC_ETC=$(pwd)/src/resources
ls -la $UC_ETC
pwd
echo HHH
echo $UC_ETC
ls -la
echo JJJJ
cp ./gradle.propertiesTravis ./gradle.properties
ls -la /home/travis/build/Activity-Insights/universal-connector/logstash-output-guardium/logstash/logstash/logstash-core/build/libs
chmod 755 ./gradlew
./gradlew test --debug
cat /home/travis/build/Activity-Insights/universal-connector/logstash-output-guardium/build/reports/tests/test/index.html