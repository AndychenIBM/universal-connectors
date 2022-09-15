#!/bin/bash

UC_PLUGIN_REPO_BRANCH=main
UC_OPENSOURCE_ROOT_DIR=universal-connectors-${UC_PLUGIN_REPO_BRANCH}
# Gather plugins to wrap in UC default offline package
grep -v '^#' defaultOfflinePackagePlugins.txt | while read -r line; do cp "${UC_OPENSOURCE_ROOT_DIR}/${line}" logstash_docker/uc/config/.; done
grep -v '^#' defaultOfflinePackagePluginsNotInOpenSource.txt | while read -r line; do cp "${UC_OPENSOURCE_ROOT_DIR}/${line}" logstash_docker/uc/config/.; done

ls -la ./logstash_docker/uc/config

docker run -e UC_OPENSOURCE_ROOT_DIR=${UC_OPENSOURCE_ROOT_DIR} --name="Lio" -v `pwd`:`pwd` -w `pwd` -dit guc_dit:latest bash
chmod -R 755 **/gradlew
docker exec Lio bash -c "./prepareUCDefaultOfflinePackageInDocker.sh"
./downloadUCBootstrapArtifact.sh
cd logstash_docker
docker build -t universal-connector .  2>&1
