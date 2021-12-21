cp **/*.gem logstash_docker/uc/config
docker  run  --name="Lio" -v `pwd`:`pwd` -w `pwd` -dit guc_dit:latest bash
chmod -R 755 **/gradlew
docker exec Lio bash -c "./buildUCDefaultOfflinePackagesInDocker.sh"
cd logstash_docker
docker pull sec-guardium-next-gen-docker-local.artifactory.swg-devops.com/universal-connector-base
docker build -t universal-connector .  2>&1
