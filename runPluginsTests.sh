#build uc-commons
cd logstash-uc-commons
chmod 755 ./gradlew
./gradlew jar
#build outputplugin
cd ../logstash-output-guardium
rm -rf logstash
mkdir logstash
cd logstash
#install ruby and clone logstash
sudo apt-get install ruby-full
git clone -b "v7.5.2" https://github.com/elastic/logstash.git
cd logstash
chmod 755 ./gradlew
./gradlew -q :logstash-core:build
#use gradlefor travis
cd ../../
cp ./gradle.propertiesTravis ./gradle.properties
#run output tests
chmod 755 ./gradlew
./gradlew test --debug
#run filter tests
cp ./gradle.propertiesTravis ../logstash-filter-s3-guardium/gradle.properties
cd ../logstash-filter-s3-guardium/
chmod 755 ./gradlew
./gradlew test --debug
#print resultreport to log
cat /home/travis/build/Activity-Insights/universal-connector/logstash-output-guardium/build/reports/tests/test/index.html