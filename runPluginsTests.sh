cd logstash-output-guardium
#use gradlefor travis
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