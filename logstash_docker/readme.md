Prerequisites:
1. docker (already installed in gmachines)


installation and configuration:
1. checkout Universal-Connector branch from https://github.ibm.com/Guardium/universal-connector
2. build the 2 .gem plugins based on - logstash-filter-mongodb-guardium and logstash-output-guardium using gradle
3. copy the .gem plugins into universal-connector/logstash_docker/config
4. login to artifactory:
    docker login  https://sec-guardium-next-gen-docker-local.artifactory.swg-devops.com/v2/ibmjava-ubi-minimal/manifests/latest
    user name: <ibm_email_addres>
    password: <artifactory_key> 
5. from logstash_docker directory, build the GUC image using the following command : docker build -t universal-connector .
    *need to make sure that the docker file contains the correct plugins' path
6. save the image on /var/IBM/Guardium/imagedata/:
    docker save universal-connector:latest | gzip > /var/IBM/Guardium/imagedata/uc.tgz


running a container- this can be done by one of the following:
1. use grdapi:
    grdapi run_local_universal_connnector debug=3
2. direct run using:
    docker run -d --name="Klaus" --network="host" -it universal-connector
    
*Logstash is set to ERROR level. in order to set another level you can set it as an env variable. for example:
 docker run -d --name="Klaus" --network="host" -e UC_LOG_LEVEL="DEBUG" -it universal-connector
 
*In order to change connectorId, use -e CONNECTOR_ID=<new_connectorId_name>. for example:
docker run -d --name="Klaus" --network="host" -e CONNECTOR_ID="123" -it universal-connector  




Project Content:
1. config directory:
	1.1. pipelines.yml- defines where to read the logstash config files from (according to the selected pipeline)
	1.2. log4j.properties- logstash configurations regarding log4j
	1.3. SnifferConfig.json- configuration for connecting with Guardium Sniffer
	1.4. UniversalConnector.json- more configuration needed for logstash's agent
	1.5. plugins- gem files that will be transferred into the container
2. pipeline directory:
	2.1. *.conf files that will be used as the configuration for logstash (including input,filter and output)
3. Dockerfile- import and install logstash, remove old version of the container, pass relevant files to the container, install plugins.
4. start_logstash.sh- this script starts logstash when "docker run..." command is executed