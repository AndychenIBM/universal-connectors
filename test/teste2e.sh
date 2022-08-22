docker logs universal-connector
docker rm -f mongo_fb
docker pull sec-guardium-next-gen-docker-local.artifactory.swg-devops.com/mongo_filebeat_ubi:travisBuild
docker run -d --name="mongo_fb" -e FILEBEAT_HOSTS="universal-connector:5044" --link universal-connector:universal-connector -it sec-guardium-next-gen-docker-local.artifactory.swg-devops.com/mongo_filebeat_ubi:travisBuild bash
cd deployments/devops-compose
./start.sh v1/universal-connector.lst pull
_MINI_SNIF_SSL_ENABLED=true _GUC_UC_LOG_LEVEL=error _GUC_PERSISTENT_QUEUE_SIZE=512mb _GUC_PIPELINE_CONFIG_PATH=/service/guc-config/TNT_7J7BDDBGQDUOYGQ5WQ3WXJ/curConfig.conf _GUC_PERSISTENT_QUEUE_TYPE=persisted _TENANT_ID=TNT_7J7BDDBGQDUOYGQ5WQ3WXJ TENANT_ID=TNT_7J7BDDBGQDUOYGQ5WQ3WXJ _MINI_SNIF_HOSTNAME=mini-snif ./start.sh v1/universal-connector.lst
echo "==========================e2e tests images status (docker images):=========================="
docker images
echo "==========================e2e tests container status (docker ps -a):=========================="
docker ps -a
# TODO: activate and stabilize e2e tests based on the existing env
exit 0
#docker cp ./test/auditLog.json mongo_fb:/data/db/auditLog.json
#docker exec -itd mongo_fb ./start.sh
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
