working directory:
	~/logstash_docker
 
 
Option1:
  build:
  	docker build -t logstash_example:1.0.0 .
  run:
  	docker run -p 5141:5141/udp <IMAGE_ID>


Option2:
  install docker-compose
  docker-compose up
  
  
  
Project Content:
1. config directory:
	1.1. logstash.yml- includes some basic configuartion regarding logstash
	1.2. GuardConsumer.properties- include configuration needed in the agent plugin
	1.3. plugins- gem files that will be transfered into the container
2. pipeline directory:
	2.1. *.conf file that will be used as the configuration for logstash (including input,filter and output)
3. docker-compose.yml- includes configurations for the docker itself (for example- ports needed to be exposed)
4. Dockerfile- import and install logstash, remove old version of the container, pass relevant files to the container, install plugin.

	
	
################## For internal use ##################
#export ARTIFACTORY_DOCKER_REPO_DOMAIN=sec-guardium-next-gen-docker-local.artifactory.swg-devops.com/
#export ARTIFACTORY_DOCKER_REPO_URL=http://sec-guardium-next-gen-docker-local.artifactory.swg-devops.com/
#export ARTIFACTORY_PASSWORD=AKCp5ccbCDpVD5gRzWPYn3H26vNyezRzdVAv9miM9QxJsKZ6XMtfk8rWT9T3UVyqHFPreZNEb
#export ARTIFACTORY_USERNAME=c3cvp8ch@ca.ibm.com
#docker login -u $ARTIFACTORY_USERNAME -p $ARTIFACTORY_PASSWORD $ARTIFACTORY_DOCKER_REPO_DOMAIN
######################################################