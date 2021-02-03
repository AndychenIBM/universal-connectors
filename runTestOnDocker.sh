#build docker image - could be taken from artifcatory
cd test
docker build -t guc_dit:latest .
#
cd ..
#docker  run  -v `pwd`:`pwd` -w `pwd` -dit guc_dit:latest bash
docker  run  --name="Alan" -v `pwd`:`pwd` -w `pwd` -dit guc_dit:latest bash
chmod -R 755 **/gradlew
docker exec Alan bash -c "./buildAllPluginsInDocker.sh"
if [ $? -eq 0 ]
then
  echo "Successfully tested and built"
else
    echo "Failed to test and build plugins"
  exit 1
fi
cp **/*.gem logstash_docker/uc/config
cd logstash_docker
docker pull sec-guardium-next-gen-docker-local.artifactory.swg-devops.com/universal-connector-base
docker build -t universal-connector .  2>&1
