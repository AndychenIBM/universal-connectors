cp **/*.gem logstash_docker/uc/config
docker  run  --name="Lio" -v `pwd`:`pwd` -w `pwd` -dit guc_dit:latest bash
chmod -R 755 **/gradlew
docker exec Lio bash -c "./prepareUCDefaultOfflinePackageInDocker.sh"
./downloadUCBootstrapArtifact.sh
cd logstash_docker
docker build -t universal-connector .  2>&1
