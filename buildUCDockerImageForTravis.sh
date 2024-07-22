#!/bin/bash

UC_PLUGIN_REPO_BRANCH=release-v1.5.7
UC_OPENSOURCE_ROOT_DIR=universal-connectors-${UC_PLUGIN_REPO_BRANCH}
UC_IMAGE_NAME="universal-connector"
# Gather plugins to wrap in UC default offline package
grep -v '^#' defaultOfflinePackagePluginsNotInOpenSource.txt | while read -r line; do cp "${UC_OPENSOURCE_ROOT_DIR}/${line}" logstash_docker/uc/config/.; done

echo 'create core_lib and copy content'
mkdir -p logstash_docker/uc/logstash-core/lib/jars/
cp ${UC_OPENSOURCE_ROOT_DIR}/common/build/libs/guardium-universalconnector-commons-*.jar logstash_docker/uc/logstash-core/lib/jars/

echo 'list content of logstash_docker/uc/logstash-core/lib/jars'
ls -ltr logstash_docker/uc/logstash-core/lib/jars/

docker run -e UC_OPENSOURCE_ROOT_DIR=${UC_OPENSOURCE_ROOT_DIR} --name="Lio" -v `pwd`:`pwd` -w `pwd` -dit guc_dit:latest bash
chmod -R 755 **/gradlew
docker exec Lio bash -c "./prepareUCDefaultOfflinePackageInDocker.sh"
./downloadUCBootstrapArtifact.sh
cd logstash_docker
docker build -qt ${UC_IMAGE_NAME} .