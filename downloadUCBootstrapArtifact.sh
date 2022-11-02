#!/bin/bash

# **************************************************************
#
# IBM Confidential
#
# OCO Source Materials
#
# 5737-L66
#
# (C) Copyright IBM Corp. 2022
#
# The source code for this program is not published or otherwise
# divested of its trade secrets, irrespective of what has been
# deposited with the U.S. Copyright Office.
#
# **************************************************************

BASE_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

# constant
VERSION=1.0.5
BOOSTRAP_LOCATION=logstash_docker/uc/setup

# main
mkdir -p $BOOSTRAP_LOCATION
echo "downloading uc-bootstrap JAR from artifactory with version: $VERSION"
wget -q --user=$ARTIFACTORY_USERNAME --password=$ARTIFACTORY_PASSWORD https://na.artifactory.swg-devops.com/artifactory/sec-guardium-next-gen-team-gradle-local/com/ibm/guardium/ucbootstrap/$VERSION/ucbootstrap-$VERSION.jar -O $BOOSTRAP_LOCATION/uc-bootstrap.jar

if [ $? -ne 0 ]; then
  echo "failed to download uc-bootstrap artifact"
  exit 1
fi;
echo "done download uc-bootstrap artifact"