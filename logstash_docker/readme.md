*before installation need to be logged in to redhat
 
installation:
  docker-compose up
  
Project Content:
1. config directory:
	1.1. logstash.yml- basic configuration regarding logstash
	1.2. pipelines.yml- defines where to read the config file from
	1.3. GuardConsumer.properties- include configuration needed in the agent plugin
	1.3. plugins- gem files that will be transferred into the container
2. pipeline directory:
	2.1. *.conf file that will be used as the configuration for logstash (including input,filter and output)
3. docker-compose.yml- includes configurations for the docker itself (for example- ports needed to be exposed)
4. Dockerfile- import and install logstash, remove old version of the container, pass relevant files to the container, install plugin.