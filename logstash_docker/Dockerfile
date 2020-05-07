FROM sec-guardium-next-gen-docker-local.artifactory.swg-devops.com/ibmjava-ubi-minimal:latest

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

COPY logstash.repo /etc/yum.repos.d/logstash.repo

RUN \
	microdnf install -y vim-enhanced && \
	microdnf install -y util-linux-ng && \
	rpm --import https://artifacts.elastic.co/GPG-KEY-elasticsearch && \
	microdnf install shadow-utils.x86_64 -y && \
	microdnf install logstash

COPY pipeline/ ${LOGSTASH_DIR}/pipeline
COPY config/ ${LOGSTASH_DIR}/config
RUN chown -R 1001:1001 ${LOGSTASH_DIR}
USER 1001
RUN .${LOGSTASH_DIR}/bin/logstash-plugin install ${LOGSTASH_DIR}/config/logstash-filter-java_filter_example-0.0.33.gem
RUN .${LOGSTASH_DIR}/bin/logstash-plugin install ${LOGSTASH_DIR}/config/logstash-output-java_output_to_guardium-1.0.2.gem
#RUN .${LOGSTASH_DIR}/bin/logstash-plugin install ${LOGSTASH_DIR}/config/logstash-input-cloudwatch_logs-1.0.3.gem
#RUN .${LOGSTASH_DIR}/bin/logstash-plugin install logstash-input-cloudwatch
ENV UDS_ETC=${LOGSTASH_DIR}/config/

#for tests:
COPY test/ /test