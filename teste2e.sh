docker stop $(docker ps -a -q) && docker rm $(docker ps -a -q)
#run docker compose
#cd /Users/itaig/docker-compose/devops-compose
cd deployments/devops-compose
./start.sh v1/buffalo.lst pull
./start.sh v1/buffalo.lst
#copy config file to mounted place
mkdir IGNORED_IN_HELM_CHARTS/guc-config/pipelines
#cp /Users/itaig/universal-connector-for-gi/tmpUcForOCPOC/universal-connector/logstash_docker/uc/customer/pipeline/gi_dummy.conf /Users/itaig/docker-compose/devops-compose/IGNORED_IN_HELM_CHARTS/guc-config/TNT_7J7BDDBGQDUOYGQ5WQ3WXJ/curConfig.conf
cp ../logstash_docker/uc/customer/pipeline/gi_dummy.conf IGNORED_IN_HELM_CHARTS/guc-config/TNT_7J7BDDBGQDUOYGQ5WQ3WXJ/curConfig.conf
#run universal connector - should be done by manager
./start.sh v1/universal-connector.lst pull
_MINI_SNIF_SSL_ENABLED=true _GUC_UC_LOG_LEVEL=error _GUC_PERSISTENT_QUEUE_SIZE=512mb _GUC_PIPELINE_CONFIG_PATH=/service/guc-config/TNT_7J7BDDBGQDUOYGQ5WQ3WXJ/curConfig.conf _GUC_PERSISTENT_QUEUE_TYPE=persisted _TENANT_ID=TNT_7J7BDDBGQDUOYGQ5WQ3WXJ TENANT_ID=TNT_7J7BDDBGQDUOYGQ5WQ3WXJ _MINI_SNIF_HOSTNAME=mini-snif ./start.sh v1/universal-connector.lst
#./start.sh v1/universal-connector.lst
#run mongo container and send traffic
#docker run -e FILEBEAT_HOSTS="universal-connector:5044" --link universal-connector:universal-connector -itd mongo_filebeat_ubi
#validate mongo container is down
docker stop mongo_fb
docker rm mongo_fb
#cd /Users/itaig/universal-connector-for-gi/tmpUcForOCPOC/universal-connector
cd ..
#run mongo docker copy audit log and run mongo in the container
#docker pull mongo image
docker pull sec-guardium-next-gen-docker-local.artifactory.swg-devops.com/mongo_filebeat_ubi:travisBuild
docker run -d --name="mongo_fb" -e FILEBEAT_HOSTS="universal-connector:5044" --link universal-connector:universal-connector -it sec-guardium-next-gen-docker-local.artifactory.swg-devops.com/mongo_filebeat_ubi:travisBuild bash
#docker run -d --name="mongo_fb" -e FILEBEAT_HOSTS="magical_chatelet:5044" --link magical_chatelet:magical_chatelet -it mongo_filebeat_ubi bash
docker cp ./test/auditLog.json mongo_fb:/data/db/auditLog.json
docker exec -itd mongo_fb ./start.sh
#wait 10 sec for finish
sleep 1000
#check mini-snif put stuff in kafka
#docker exec -itd kafka /opt/bitnami/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic ingest --from-beginning .
#docker exec -it kafka /opt/bitnami/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic ingest --from-beginning .  --timeout-ms 10000
#check the number of messages contains the db name
docker logs universal-connector
docker exec -it universal-connector env
nextNum=`docker exec -it kafka /opt/bitnami/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic ingest --from-beginning .  --timeout-ms 10000 | grep e2edb | wc -l`
if [[ $nextNum -eq 11 ]]
then
  echo "ok"
  exit 0
else
  echo "wrong number of messages in kafka :$nextNum"
  exit 1
fi