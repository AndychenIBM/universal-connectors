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
RUN microdnf -y install vim procps findutils &&\
    microdnf clean all

#Copy necessary files
COPY pipeline/ ${LOGSTASH_DIR}/pipeline
COPY config/ ${LOGSTASH_DIR}/config

RUN chown -R 1001:1001 ${LOGSTASH_DIR}
USER 1001

#Install plugins
RUN .${LOGSTASH_DIR}/bin/logstash-plugin install ${LOGSTASH_DIR}/config/logstash-filter-java_filter_example-0.2.1.gem
RUN .${LOGSTASH_DIR}/bin/logstash-plugin install ${LOGSTASH_DIR}/config/logstash-output-java_output_to_guardium-1.0.5.gem
#RUN .${LOGSTASH_DIR}/bin/logstash-plugin install ${LOGSTASH_DIR}/config/logstash-input-cloudwatch_logs-1.0.3.gem

#Define environment variables
ENV UDS_ETC=${LOGSTASH_DIR}/config/
ENV PATH ${LOGSTASH_DIR}/bin:$PATH

#for tests:
#COPY test/ /test

CMD ["logstash"]