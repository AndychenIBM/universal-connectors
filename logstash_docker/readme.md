working directory:
	~/logstash_docker
 
installation:
  docker-compose up
  
Project Content:
1. config directory:
	1.1. pipelines.yml- defines where to read the logstash config files from (according to the selected pipeline)
	1.2. log4j.properties- logstash configurations regarding log4j
	1.3. SnifferConfig.json- configuration for connecting with Guardium Sniffer
	1.4. UniversalConnector.json- more configuration needed for logstash's agent
	1.5. plugins- gem files that will be transferred into the container
2. pipeline directory:
	2.1. *.conf file that will be used as the configuration for logstash (including input,filter and output)
3. docker-compose.yml- includes configurations for the docker itself (for example- ports needed to be exposed)
4. Dockerfile- import and install logstash, remove old version of the container, pass relevant files to the container, install plugins.