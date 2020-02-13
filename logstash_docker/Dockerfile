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

FROM registry.access.redhat.com/ubi8/ubi

RUN yum install -y wget openssl ca-certificates gzip tar \
    && yum update; yum clean all
	
LABEL maintainer="IBM Security" \
    #build_date=
    #version=
    #release=
    name="logstash" \
    summary="Image for logstash with IBM java on UBI" \
    description="This image contains logstash with IBM JAVA install on Red Hat UBI as the base OS. For more information on this image please see documentation files" \
    vendor="IBM Corp."

ENV JAVA_VERSION 1.8.0_sr6

RUN set -eux; \
    useradd -u 1001 -r -g 0 -s /usr/sbin/nologin default; \
    ARCH="$(uname -m)"; \
    case "${ARCH}" in \
       amd64|x86_64) \
         ESUM='4c3b62dc988223239d0049e03c40444524356e3cdf805972e1a35d127d00c1c9'; \
         YML_FILE='sdk/linux/x86_64/index.yml'; \
         ;; \
       ppc64el|ppc64le) \
         ESUM='18c2eccf99225e6e7643141d8da4110cacc39f2fa00149fc26341d2272cc0102'; \
         YML_FILE='sdk/linux/ppc64le/index.yml'; \
         ;; \
       s390x) \
         ESUM='97e45a2fffa1ec8c57da614313cbb32d430210ae18e22c7022cd18204ea5ec96'; \
         YML_FILE='sdk/linux/s390x/index.yml'; \
         ;; \
       *) \
         echo "Unsupported arch: ${ARCH}"; \
         exit 1; \
         ;; \
    esac; \
    BASE_URL="https://public.dhe.ibm.com/ibmdl/export/pub/systems/cloud/runtimes/java/meta/"; \
    wget -q -U UA_IBM_JAVA_Docker -O /tmp/index.yml ${BASE_URL}/${YML_FILE}; \
    JAVA_URL=$(sed -n '/^'${JAVA_VERSION}:'/{n;s/\s*uri:\s//p}'< /tmp/index.yml); \
    wget -q -U UA_IBM_JAVA_Docker -O /tmp/ibm-java.bin ${JAVA_URL}; \
    echo "${ESUM}  /tmp/ibm-java.bin" | sha256sum -c -; \
    echo "INSTALLER_UI=silent" > /tmp/response.properties; \
    echo "USER_INSTALL_DIR=/opt/ibm/java" >> /tmp/response.properties; \
    echo "LICENSE_ACCEPTED=TRUE" >> /tmp/response.properties; \
    mkdir -p /opt/ibm; \
    chmod +x /tmp/ibm-java.bin; \
    /tmp/ibm-java.bin -i silent -f /tmp/response.properties; \
    rm -f /tmp/response.properties; \
    rm -f /tmp/index.yml; \
    mkdir -p /licenses; \
    cp /opt/ibm/java/license_en.txt /licenses; \
    chown -R 1001:0 /opt/ibm/java; \
    rm -f /tmp/ibm-java.bin;

ENV JAVA_HOME=/opt/ibm/java/jre \
    PATH=/opt/ibm/java/bin:$PATH \
    IBM_JAVA_OPTIONS="-XX:+UseContainerSupport"
	
ENV LOGSTASH_VERSION=7.5.1
ENV LOGSTASH_DIR=/usr/share/logstash

RUN \
  rpm --rebuilddb && yum clean all && \
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


