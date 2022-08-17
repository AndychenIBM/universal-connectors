echo $(pwd)
# TODO- extract list of plugins to be copies to a text file
cp ./universal-connectors-main/filter-plugin/logstash-output-guardium/logstash-output-java_output_to_guardium-1.3.9.gem ./logstash_docker/uc/config/.
#cp ./logstash_docker/uc/config/logstash-input-cloudwatch_logs-1.0.3.gem ./logstash_docker/uc/config/.
cp ./universal-connectors-main/filter-plugin/logstash-filter-mysql-guardium/logstash-filter-mysql_filter_guardium-1.0.0.gem ./logstash_docker/uc/config/.
cp ./universal-connectors-main/filter-plugin/logstash-filter-mongodb-guardium/logstash-filter-mongodb_guardium_filter-0.6.6.gem ./logstash_docker/uc/config/.
cp ./universal-connectors-main/filter-plugin/logstash-filter-s3-guardium/logstash-filter-logstash_filter_s3_guardium-0.5.4.gem ./logstash_docker/uc/config/.

ls -la ./logstash_docker/uc/config
docker  run  --name="Lio" -v `pwd`:`pwd` -w `pwd` -dit guc_dit:latest bash
chmod -R 755 **/gradlew
docker exec Lio bash -c "./prepareUCDefaultOfflinePackageInDocker.sh"
./downloadUCBootstrapArtifact.sh
cd logstash_docker
docker build -t universal-connector .  2>&1
