Prerequisites:
1. docker (already installed on gmachines)


installation and configuration:
1. checkout Universal-Connector branch from https://github.ibm.com/Guardium/universal-connector
2. build the .gem plugins
3. copy the .gem plugins into universal-connector/logstash_docker/uc/config
4. login to artifactory:
    docker login  https://sec-guardium-next-gen-docker-local.artifactory.swg-devops.com/v2/ibmjava-ubi-minimal/manifests/latest
    user name: <ibm_email_addres>
    password: <artifactory_key> 
5. from logstash_docker directory, build the GUC image using the following command : docker build -t universal-connector .
    *need to make sure that the docker file contains the correct plugins path
6. save the image on /var/IBM/Guardium/imagedata/:
    docker save universal-connector:latest | gzip > /var/IBM/Guardium/imagedata/uc.tgz


running a container- this can be done by one of the following:
1. use grdapi:
    grdapi run_local_universal_connector debug=3
2. direct run using:
    docker run -d --name="Klaus" --network="host" -it universal-connector
    
*Logstash is set to error level. in order to set another level you can set it as an env variable. for example:
 docker run -d --name="Klaus" --network="host" -e UC_LOG_LEVEL="debug" -it universal-connector
 
*In order to change connectorId, use -e CONNECTOR_ID=<new_connectorId_name>. for example:
docker run -d --name="Klaus" --network="host" -e CONNECTOR_ID="123" -it universal-connector  

*In order to change listening port, use -e <protocol_type>=<new_listening_port>. for example:
docker run -d --name="Klaus" --network="host" -e UDP_PORT="514" -it universal-connector  
docker run -d --name="Klaus" --network="host" -e TCP_PORT="500" -it universal-connector  
docker run -d --name="Klaus" --network="host" -e FILEBEAT_PORT="5044" -it universal-connector  


Project Content:
1. uc directory:
    1.1. config directory:
	    1.1.1. pipelines.yml- defines where to read the Logstash config files from (according to the selected pipeline)
	    1.1.2. log4j.properties- Logstash configurations regarding log4j
	    1.1.3. SnifferConfig.json- configuration for connecting with Guardium Sniffer
	    1.1.4. UniversalConnector.json- more configuration needed for Logstash's agent
	    1.1.5. plugins- gem files that will be transferred into the container
    1.2. pipeline directory:
	    1.2.1. *.conf files that will be used as the configuration for Logstash (including input,filter and output)
	1.3. scripts directory:
	    1.3.1. *.sh scripts for universal-connector usage
	1.4. pipelines.yml- configure pipelines for Logstash
2. uc_perf_tests directory- a directory meant for performance tests
3. Dockerfile- import and install Logstash, remove old version of the container, pass relevant files to the container, install plugins.