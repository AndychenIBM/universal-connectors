#build docker image - could be taken from artifcatory
chmod -R 777 *
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