FROM registry.redhat.io/ubi8/ubi-minimal:8.2

LABEL maintainer="IBM Security" \
    #build_date=
    #version=
    #release=
    name="Universal-Connector Base Image" \
    summary="Base image for Guardium Universal Connector" \
    description="This image contains Logstash running on Java OpenJDK, installed on UBI operating system. For more information on this image please see documentation files" \
    vendor="IBM Corp."

ENV JAVA_HOME=/opt/java/openjdk \
    PATH="/opt/java/openjdk/bin:$PATH" \
    JAVA_VERSION="jdk-11.0.8+10_openj9-0.21.0" \
    LOGSTASH_VERSION=7.5.2 \
    LOGSTASH_DIR=/usr/share/logstash \
    LOG_GUC_DIR=/var/log/uc/ \
    UC_EXTERNAL_CONFIG=/etc/uc/conf


#Install necessary packages
RUN microdnf install -y tzdata openssl curl ca-certificates procps shadow-utils.x86_64 tar gzip hostname vim && \
    microdnf update -y; microdnf clean all && \
#Download and install OpenJDK
    set -eux; \
    ARCH="$(uname -m)"; \
    case "${ARCH}" in \
        #TODO- consider adding binaries or aarch64/arm64, ppc64el/ppc64le, s390x
       amd64|x86_64) \
         ESUM='7cc4b24e2ac0ed26bf3ec31241bde65b672952bf0df5e1606b263fadcb21b3b7'; \
         BINARY_URL='https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.8%2B10_openj9-0.21.0/OpenJDK11U-jre_x64_linux_openj9_11.0.8_10_openj9-0.21.0.tar.gz'; \
         ;; \
       *) \
         echo "Unsupported arch: ${ARCH}"; \
         exit 1; \
         ;; \
    esac; \
    curl -LfsSo /tmp/openjdk.tar.gz ${BINARY_URL}; \
    echo "${ESUM} */tmp/openjdk.tar.gz" | sha256sum -c -; \
    mkdir -p /opt/java/openjdk; \
    cd /opt/java/openjdk; \
    tar -xf /tmp/openjdk.tar.gz --strip-components=1; \
    rm -rf /tmp/openjdk.tar.gz && \
#Change ownership to guc
    groupadd -g 991 guardium && useradd -G guardium -u 1001 guc && \
    mkdir -p ${LOGSTASH_DIR} ${LOG_GUC_DIR} ${UC_EXTERNAL_CONFIG} && chown -R guc:guardium ${LOGSTASH_DIR} ${LOG_GUC_DIR} ${UC_EXTERNAL_CONFIG}

#Change User to guc
USER guc

#Download and extract logstash
RUN curl https://artifacts.elastic.co/downloads/logstash/logstash-oss-${LOGSTASH_VERSION}.tar.gz -o ${LOGSTASH_DIR}/logstash-oss-${LOGSTASH_VERSION}.tar.gz && \
	tar -xzf ${LOGSTASH_DIR}/logstash*.tar.gz  -C ${LOGSTASH_DIR} --strip-components=1 && \
	rm ${LOGSTASH_DIR}/*.tar.gz && \
#Remove unnecessary heavy plugins
    cd $LOGSTASH_DIR/vendor/bundle/jruby/2.5.0/gems && \
    rm -rf logstash-filter-geoip-6.0.3-java logstash-input-snmp-1.2.1 \
    logstash-input-azure-* logstash-integration-kafka-* logstash-input-http-* nokogiri-1.10.7-java \
    sequl-5.27.0 backports-3.15.0 tzinfo-data-1.2019.3 snappy-0.0.12-java \
    mail-2.6.6 logstash-input-azure_event_hubs-1.1.2 logstash-filter-jdbc_static-1.0.7 snmp-1.3.2