# **************************************************************
#
# IBM Confidential
#
# OCO Source Materials
#
# 5737-L66
#
# (C) Copyright IBM Corp. 2019
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
NAME=reports-runner
DOCKER_START_SCRIPT=./start.sh
DOCKER_LST=v1/reports-runner.lst
DEVOPS_COMPOSE_DIR=devops-compose
DEVOPS_COMPOSE_ARGS=-f ../test/docker-compose.override.yml
DEVOPS_COMPOSE_PULL=pull
DEVOPS_COMPOSE_UP=up -d
DEVOPS_COMPOSE_DOWN=down
ARTIFACTORY_DOCKER_REPO_DOMAIN=sec-guardium-next-gen-docker-local.artifactory.swg-devops.com/
DOCKER_IMAGE_TAG=latest
.PHONY:all build test check clean run dockerBuild dockerRun dockerStop
# To build all:
all: build test check run



# To clean the reports-runner build env
clean:
#	./gradlew clean

# To build reports-runner service dev local runtime
build:
#	clean
#	./buildPlugins.sh
#    ./gradlew .
#	./gradlew installDist

# To run git submodule update --init --recursive --remote
submodule:
	git submodule update --init --recursive --remote

# To run unit-test:
test:
#	./gradlew test

# To run e2e test:
test_e2e:
#	./gradlew test_e2e

# To run unit-test and JaCoCo code coverage script:
check:
#	./gradlew check

# To run just JaCoCo code coverage script, make test will be executed first.
report:  test
#	./gradlew jacocoTestReport

# To run the server for production use:
run:
#	./build/install/reports-runner/build/common-reports-runner-server

# To build docker image with the latest build files/libs

dockerBuild:
	./buildDocketForTravis.sh

#	./dockerBuild/package/build.sh
#	docker tag ${NAME} ${ARTIFACTORY_DOCKER_REPO_DOMAIN}${NAME}:${DOCKER_IMAGE_TAG}

# To run reports-runner inside docker container
dockerRun:
#	cd ${DEVOPS_COMPOSE_DIR} && ${DOCKER_START_SCRIPT} ${DOCKER_LST} ${DEVOPS_COMPOSE_PULL}
#	cd ${DEVOPS_COMPOSE_DIR} && ${DOCKER_START_SCRIPT} ${DOCKER_LST} ${DEVOPS_COMPOSE_ARGS} ${DEVOPS_COMPOSE_UP}


# To stop the reports-runner container

dockerStop:
#	cd ${DEVOPS_COMPOSE_DIR} && ${DOCKER_START_SCRIPT} ${DOCKER_LST} ${DEVOPS_COMPOSE_ARGS} ${DEVOPS_COMPOSE_DOWN}
