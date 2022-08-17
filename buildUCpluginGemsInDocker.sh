# Build docker image - could be taken from artifactory
chmod -R 777 *
cd test
docker build -t guc_dit:latest .
#
cd ..
pwd
# Pull UC plugins from open-source repo
rm -rf universal-connectors-main main.zip universal-connectors-main
wget https://github.com/IBM/universal-connectors/archive/refs/heads/main.zip && unzip -q main.zip
cp -R ./logstash-output-guardium ./universal-connectors-main/filter-plugin/logstash-output-guardium
chmod -R 777 universal-connectors-main
ls -la ./universal-connectors-main/filter-plugin/

# Run UC build container to build plugins inside it
docker run  --name="Alan" -v `pwd`:`pwd` -w `pwd` -dit guc_dit:latest bash
chmod -R 755 **/gradlew
docker exec Alan bash -c "./buildUCPluginGems.sh"
if [ $? -eq 0 ]
then
  echo "Successfully tested and built"
else
  echo "Failed to test and build plugins"
  exit 1
fi