CURDIR=`pwd`
cp logstash_docker/uc/config/*.gem /usr/share/logstash/config
cd /usr/share/logstash/config
#../bin/logstash-plugin install logstash-filter-mysql_percona_guardium_filter-1.0.6.gem
../bin/logstash-plugin list --verbose
../bin/logstash-plugin install logstash-input-cloudwatch_logs-1.0.3.gem
../bin/logstash-plugin install logstash-filter-logstash_filter_s3_guardium-0.5.3.gem
#../bin/logstash-plugin install logstash-output-java_output_to_guardium-1.3.2.gem
../bin/logstash-plugin install logstash-filter-mongodb_guardium_filter-0.6.6.gem
../bin/logstash-plugin install logstash-filter-mysql_filter_guardium-1.0.0.gem
../bin/logstash-plugin prepare-offline-pack --output ./guardium_logstash-offline-plugins.zip --overwrite logstash-filter-logstash_filter_s3_guardium logstash-filter-mysql_filter_guardium logstash-filter-mongodb_guardium_filter logstash-input-cloudwatch_logs
../bin/logstash-plugin list --verbose
cp ./guardium_logstash-offline-plugins.zip $CURDIR/logstash_docker/uc/config/
