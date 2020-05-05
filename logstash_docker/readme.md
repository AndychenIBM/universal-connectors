Prerequisites:
1. docker (already installed in gmachines)
2. docker-compose (installation instructions: https://docs.docker.com/compose/install)



installation and configuration:
1. copy logstash_docker directory in the machine
2. optional- make any adjustments needed in logstash's *.conf file placed in logstash_docker/pipeline/
3. configure agent in logstash_docker/config:
    3.1. change ip address in SnifferConfig.json to the sniffer machine's address
    3.2. optional- change connectorId UniversalConnector.json file
4. login to artifactory:
    docker login  https://sec-guardium-next-gen-docker-local.artifactory.swg-devops.com/v2/ibmjava-ubi-minimal/manifests/latest
    user name: <ibm_email_addres>
    password: <artifactory_key> 
3. enter logstash_docker directory and type: docker-compose up

  

install a new plugin:
1. enter the new .gem file to logstash_docker/config
2. edit/add the installation to Dockerfile:
    RUN .${LOGSTASH_DIR}/bin/logstash-plugin install ${LOGSTASH_DIR}/config/<plugin_name>
3. rebuild docker: docker-compose up --build


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
5. logstash.repo- needed in order to install the docker inside ubi