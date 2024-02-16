# Universal-Connector

[![Build Status](https://travis.ibm.com/Activity-Insights/Universal-connector.svg?token=8TG9RpY8ENudrXBeAyyn&branch=master)](https://travis.ibm.com/Activity-Insights/universal-connector)

Guardium Next Gen Universal-Connector

## Getting Started

### Prerequisites

Be sure to add your email and password in ENV variables in ~/.bashrc or ~/.bash_profile (macos)
this is necessary to fetch Jar file dependencies from the IBM artifactory
do docker login twice, one for docker hub, one for artifactory.

```
export ARTIFACTORY_USERNAME="my_ibm_email_address"
export ARTIFACTORY_PASSWORD="my_ibm_p@ssw0rd"

docker login --username=c3cvp8ch --password=************** >/dev/null 2>&1
docker login --username=c3cvp8ch@ca.ibm.com --password=************** http://sec-guardium-next-gen-docker-local.artifactory.swg-devops.com  >/dev/null 2>&1

add your current user to docker group, see docker online help ...
```

## Installing Universal Connector plug-ins

```bash
$ make build
```

## Updating submodules

```bash
$ make submodule
```

## Build UC image

```bash
$ make dockerBuild
```

## Running tests

### To run unit-test:

```bash
$ make test
```

### To run end-to-end tests - *** currently not available ***:
To run end to end tests, you'll need to startup the containers of this project, by running `make dockerRun`, and by staring up apigateway, if needed, thru separate devops-compose project. Make sure you defined the required environment variables needed for these tests, like AUTH_ADMIN_USER and AUTH_ADMIN_CREDENTIAL, as defined in devops-compose/.envForUnitTest.

```bash
$ make test_e2e
```

### To test new uc-bootstrap
If you developed new uc-bootstrap and want to test it please do the following: 

Connect to Guardium Insights cli with oc login command

run
```bash
$ oc get pods | grep universal-connector
$ oc debug <universal-connector-pod-name-from-response>
```
There will be created a new pod with the same configuration for debug purposes, and it will terminate once you exit it.

Inside this pod you can run the script that downloads new uc-bootstrap from artifactory and runs it. 

arguments must include artifactory name and artifactory token

optional arguments: version of uc bootstrap to download, -o means overwrite current bootstrap, id of the UC Connection that you want to get the setup for.
```bash
./scripts/get_custom_bootstrap.sh -u <uname> -token <pass> -v <version> -id <connection> -o 
```