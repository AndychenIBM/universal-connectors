# (C) Copyright IBM Corporation 2016, 2020
#
# ------------------------------------------------------------------------------
#               NOTE: THIS DOCKERFILE IS GENERATED VIA "update.sh"
#
#                       PLEASE DO NOT EDIT IT DIRECTLY.
# ------------------------------------------------------------------------------
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

FROM registry.connect.redhat.com/ibm/ibmjava8

LABEL maintainer="IBM Security" \
    #build_date=
    #version=
    #release=
    name="logstash" \
    summary="Image for logstash with IBM java on UBI" \
    description="This image contains logstash with IBM JAVA install on Red Hat UBI as the base OS. For more information on this image please see documentation files" \
    vendor="IBM Corp."

ENV LOGSTASH_VERSION=7.5.1
ENV LOGSTASH_DIR=/usr/share/logstash

USER root

RUN \
  #rpm --rebuilddb && yum clean all && \
  mkdir -p ${LOGSTASH_DIR} && \
  cd ${LOGSTASH_DIR} && \
  curl -O https://artifacts.elastic.co/downloads/logstash/logstash-${LOGSTASH_VERSION}.tar.gz && \
  tar zxvf logstash-${LOGSTASH_VERSION}.tar.gz -C ${LOGSTASH_DIR} --strip-components=1 && \
  rm -f logstash-${LOGSTASH_VERSION}.tar.gz && \
  yum clean all -y

COPY pipeline/ ${LOGSTASH_DIR}/pipeline
COPY config/ ${LOGSTASH_DIR}/config
RUN chown -R 1001:1001 ${LOGSTASH_DIR}
USER 1001
RUN .${LOGSTASH_DIR}/bin/logstash-plugin install ${LOGSTASH_DIR}/config/logstash-filter-java_filter_example-0.0.1.gem
#RUN .${LOGSTASH_DIR}/bin/logstash-plugin install ${LOGSTASH_DIR}/config/logstash-output-java_output_example-1.0.0.gem