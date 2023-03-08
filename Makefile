# **************************************************************
#
# IBM Confidential
#
# OCO Source Materials
#
# 5737-L66
#
# (C) Copyright IBM Corp. 2019, 2023
#
# The source code for this program is not published or otherwise
# divested of its trade secrets, irrespective of what has been
# deposited with the U.S. Copyright Office.
#
# **************************************************************

# Ticketing
#Guardium Next Gen Ticketing microservice is responsible for CRUD operations for tickets on external services
# Be sure to add your email and password in ENV variables in ~/.bashrc
#  this is necessary to fetch Jar file dependencies from the IBM artifactory
NAME=universal-connector
DOCKER_START_SCRIPT=./start.sh
DOCKER_LST=v1/universal-connector.lst
DEVOPS_COMPOSE_DIR=deployments/devops-compose
DEVOPS_COMPOSE_ARGS=-f ../../test/docker-compose.override.yml
DEVOPS_COMPOSE_PULL=pull
DEVOPS_COMPOSE_UP=up -d
DEVOPS_COMPOSE_DOWN=down
ARTIFACTORY_DOCKER_REPO_DOMAIN=docker-na-public.artifactory.swg-devops.com/sec-guardium-next-gen-docker-local/
DOCKER_IMAGE_TAG=latest
.PHONY:build dockerBuild test test_e2e dockerRun dockerStop

# Build UC plugins
build:
	./buildUCpluginGemsInDocker.sh

# Pull submodules from Git
submodule:
	git submodule update --init --recursive

# To run unit-test:
test:
	chmod -R 755 ./test
	./test/test.sh

# To run e2e test:
test_e2e:
	./test/teste2e.sh

# Build UC docker image
dockerBuild:
	./buildUCDockerImageForTravis.sh

# To run universal-connector inside docker container
dockerRun:
	cd ${DEVOPS_COMPOSE_DIR} && ${DOCKER_START_SCRIPT} ${DOCKER_LST} ${DEVOPS_COMPOSE_PULL}
	cd ${DEVOPS_COMPOSE_DIR} && ${DOCKER_START_SCRIPT} ${DOCKER_LST} ${DEVOPS_COMPOSE_ARGS} ${DEVOPS_COMPOSE_UP}

# To stop the universal-connector container
dockerStop:
	cd ${DEVOPS_COMPOSE_DIR} && ${DOCKER_START_SCRIPT} ${DOCKER_LST} ${DEVOPS_COMPOSE_ARGS} ${DEVOPS_COMPOSE_DOWN}