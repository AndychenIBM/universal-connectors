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

#Download and extract logstash
RUN wget https://artifacts.elastic.co/downloads/logstash/logstash-${LOGSTASH_VERSION}.tar.gz --no-verbose && \
	mkdir -p ${LOGSTASH_DIR} && \
	tar -xzf logstash*.tar.gz  -C ${LOGSTASH_DIR} --strip-components=1 && \
	rm *.tar.gz

#Download basic libraries
RUN microdnf -y install vim procps findutils && microdnf clean all

#Copy necessary files
COPY pipeline/ ${LOGSTASH_DIR}/pipeline
COPY config/ ${LOGSTASH_DIR}/config

RUN chown -R 1001:1001 ${LOGSTASH_DIR}
USER 1001

#Install plugins
RUN .${LOGSTASH_DIR}/bin/logstash-plugin install ${LOGSTASH_DIR}/config/logstash-filter-java_filter_example-0.3.0.gem
RUN .${LOGSTASH_DIR}/bin/logstash-plugin install ${LOGSTASH_DIR}/config/logstash-output-java_output_to_guardium-1.0.5.gem
#RUN .${LOGSTASH_DIR}/bin/logstash-plugin install ${LOGSTASH_DIR}/config/logstash-input-cloudwatch_logs-1.0.3.gem

#Define environment variables
ENV UDS_ETC=${LOGSTASH_DIR}/config/
ENV PATH ${LOGSTASH_DIR}/bin:$PATH

#Clear cache and unnecessary heavy plugins
RUN rm -rf $LOGSTASH_DIR/vendor/cache/*.gem $LOGSTASH_DIR/vendor/bundle/jruby/2.5.0/cache/*.gem && \
    cd $LOGSTASH_DIR/vendor/bundle/jruby/2.5.0/gems && \
    rm -rf logstash-filter-geoip-6.0.3-java logstash-input-snmp-1.2.1 aws-sdk-* \
    logstash-input-azure-* logstash-integration-kafka-* logstash-input-http-* nokogiri-1.10.7-java \
    sequel-5.27.0 backports-3.15.0 tzinfo-data-1.2019.3 snappy-0.0.12-java \
    mail-2.6.6 logstash-input-azure_event_hubs-1.1.2 logstash-filter-jdbc_static-1.0.7 snmp-1.3.2

CMD ["logstash"]