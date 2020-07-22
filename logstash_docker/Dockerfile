FROM ibmjava:8-jre-alpine
#1. consider using ibmjava:sfj-alpine as a base image, if no ScriptEngine for JavaScript is needed
#2. for GI base imaege:
#   1. use the following base image: sec-guardium-next-gen-docker-local.artifactory.swg-devops.com/ibmjava-ubi-minimal:latest
#   2. remove 'apk add --no-cache bash' from RUN command
#   3. add basic libraries support for ubi: 'microdnf -y install vim procps findutils && microdnf clean all'

LABEL maintainer="IBM Security" \
    #build_date=
    #version=
    #release=
    name="logstash" \
    summary="Image for logstash based on ibmjava8-jre-alpine" \
    description="This image contains logstash with IBM jre installed based on alpine environment. For more information on this image please see documentation files" \
    vendor="IBM Corp."

ENV LOGSTASH_VERSION=7.5.0
ENV LOGSTASH_DIR=/usr/share/logstash

#Copy necessary files
COPY uc/. ${LOGSTASH_DIR}/

ENV LOGSTASH_VERSION=7.5.0
ENV UDS_ETC=${LOGSTASH_DIR}/config/
ENV UC_ETC=${LOGSTASH_DIR}/config/
ENV UC_SCRIPTS=${LOGSTASH_DIR}/scripts/

#Download bash
RUN apk add --no-cache bash && \
#Change owner
    chown -R 1001:1001 ${LOGSTASH_DIR}

#Change User to 1001
USER 1001

#Download and extract logstash
RUN	wget -q https://artifacts.elastic.co/downloads/logstash/logstash-${LOGSTASH_VERSION}.tar.gz -P ${LOGSTASH_DIR} && \
	tar -xzf ${LOGSTASH_DIR}/logstash*.tar.gz  -C ${LOGSTASH_DIR} --strip-components=1 && \
	rm ${LOGSTASH_DIR}/*.tar.gz && \
#Install plugins
	.${LOGSTASH_DIR}/bin/logstash-plugin install ${LOGSTASH_DIR}/config/logstash-filter-java_filter_example-0.4.2.gem && \
    .${LOGSTASH_DIR}/bin/logstash-plugin install ${LOGSTASH_DIR}/config/logstash-output-java_output_to_guardium-1.1.0.gem && \
    .${LOGSTASH_DIR}/bin/logstash-plugin install ${LOGSTASH_DIR}/config/logstash-input-cloudwatch_logs-1.0.3.gem && \
    .${LOGSTASH_DIR}/bin/logstash-plugin install ${LOGSTASH_DIR}/config/logstash-filter-logstash_filter_s3_guardium-0.3.6.gem && \
#Clear cache and unnecessary heavy plugins:
    rm -rf $LOGSTASH_DIR/vendor/cache/*.gem $LOGSTASH_DIR/vendor/bundle/jruby/2.5.0/cache/*.gem && \
    cd $LOGSTASH_DIR/vendor/bundle/jruby/2.5.0/gems && \
    rm -rf logstash-filter-geoip-6.0.3-java logstash-input-snmp-1.2.1 \
    logstash-input-azure-* logstash-integration-kafka-* logstash-input-http-* nokogiri-1.10.7-java \
    sequl-5.27.0 backports-3.15.0 tzinfo-data-1.2019.3 snappy-0.0.12-java \
    mail-2.6.6 logstash-input-azure_event_hubs-1.1.2 logstash-filter-jdbc_static-1.0.7 snmp-1.3.2 && \
#Create customer dir
    mkdir -p ${LOGSTASH_DIR}/customer/pipeline ${LOGSTASH_DIR}/customer/config && \
#Make sure user can read,write and execute the scripts
    chmod 755 ${UC_SCRIPTS}/*.sh && \
#Make sure scripts are not uploaded with windows characters additions
    sed -i -e 's/\r$//' ${LOGSTASH_DIR}/scripts/*.sh

#TODO- remove these line and make sure tar command doesn't override pipelines.yml file
COPY uc/config/pipelines.yml ${LOGSTASH_DIR}/config/pipelines.yml

ENV PATH ${LOGSTASH_DIR}/bin:$PATH

CMD ${UC_SCRIPTS}/start_logstash.sh
