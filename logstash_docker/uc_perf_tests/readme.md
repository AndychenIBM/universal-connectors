prerequisites:
1. universal-connector:latest image loaded in docker
2. make sure test_logs/1.log exists and contains example log
3. if needed, use duplicate_log_file.sh script to duplicate 1.log 50 times:
	from uc_perf_test directory type: sh duplicate_log_file.sh


build: 
	docker build -t universal-connector_perf_tests .
run: 
	docker run -d --name="perf" --network="host" -it universal-connector_perf_tests bash
exec: 
	docker exec -it perf bash


how to run basic tests:
1. choose or create a conf file for logstash from $LOGSTASH_DIR/pipeline directory:
	logstash -f $LOGSTASH_DIR/pipeline/<conf file>
2. enter this container from another session and check the events/sec:
	docker exec -it perf bash
	tail -f /tmp/logstash_plugin/log/events_per_sec-<today's_date>.log
	 
	 
	 
*if needed, change workers using -w flag and batch size using -b flag
	 
	 
