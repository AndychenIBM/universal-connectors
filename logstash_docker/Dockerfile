FROM ibmjava:8-jre-alpine
#TODO- consider using ibmjava:sfj-alpine as a base image, if no ScriptEngine for JavaScript is needed

LABEL maintainer="IBM Security" \
    #build_date=
    #version=
    #release=
    name="logstash" \
    summary="Image for logstash based on ibmjava8-jre-alpine" \
    description="This image contains logstash with IBM jre installed based on alpine environment. For more information on this image please see documentation files" \
    vendor="IBM Corp."

ENV LOGSTASH_VERSION=7.5.1
ENV LOGSTASH_DIR=/usr/share/logstash

USER root

#Download and extract logstash
RUN apk add --no-cache bash && \
	wget -q https://artifacts.elastic.co/downloads/logstash/logstash-${LOGSTASH_VERSION}.tar.gz && \
	mkdir -p ${LOGSTASH_DIR} && \
	tar -xzf logstash*.tar.gz  -C ${LOGSTASH_DIR} --strip-components=1 && \
	rm *.tar.gz && \
#change owner
	chown -R 1001:1001 ${LOGSTASH_DIR}

#Copy necessary files
COPY --chown=1001 pipeline/ ${LOGSTASH_DIR}/pipeline
COPY --chown=1001 config/ ${LOGSTASH_DIR}/config
COPY --chown=1001 start_logstash.sh /start_logstash.sh

USER 1001

#Install plugins
RUN	.${LOGSTASH_DIR}/bin/logstash-plugin install ${LOGSTASH_DIR}/config/logstash-filter-java_filter_example-0.3.0.gem && \
    .${LOGSTASH_DIR}/bin/logstash-plugin install ${LOGSTASH_DIR}/config/logstash-output-java_output_to_guardium-1.0.5.gem && \
#    .${LOGSTASH_DIR}/bin/logstash-plugin install ${LOGSTASH_DIR}/config/logstash-input-cloudwatch_logs-1.0.3.gem && \
#Clear cache and unnecessary heavy plugins:
    rm -rf $LOGSTASH_DIR/vendor/cache/*.gem $LOGSTASH_DIR/vendor/bundle/jruby/2.5.0/cache/*.gem && \
    cd $LOGSTASH_DIR/vendor/bundle/jruby/2.5.0/gems && \
    rm -rf logstash-filter-geoip-6.0.3-java logstash-input-snmp-1.2.1 aws-sdk-* \
    logstash-input-azure-* logstash-integration-kafka-* logstash-input-http-* nokogiri-1.10.7-java \
    sequel-5.27.0 backports-3.15.0 tzinfo-data-1.2019.3 snappy-0.0.12-java \
    mail-2.6.6 logstash-input-azure_event_hubs-1.1.2 logstash-filter-jdbc_static-1.0.7 snmp-1.3.2

ENV UDS_ETC=${LOGSTASH_DIR}/config/
ENV PATH ${LOGSTASH_DIR}/bin:$PATH

CMD /start_logstash.sh