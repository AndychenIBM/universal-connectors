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
