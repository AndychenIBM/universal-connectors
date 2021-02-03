cp **/*.gem logstash_docker/uc/config
cd logstash_docker
docker pull sec-guardium-next-gen-docker-local.artifactory.swg-devops.com/universal-connector-base
docker build -t universal-connector .  2>&1