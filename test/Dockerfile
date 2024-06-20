# ***************************************************************** */
#                                                                   */
# IBM Confidential                                                  */
#                                                                   */
# OCO Source Materials                                              */
#                                                                   */
# 5737-L66                                                          */
#                                                                   */
# Copyright IBM Corp. 2019, 2024                                    */
#                                                                   */
# The source code for this program is not published or otherwise    */
# divested of its trade secrets, irrespective of what has been      */
# deposited with the U.S. Copyright Office.                         */
#                                                                   */
# ***************************************************************** */

# docker image version
ARG ibm_semeru_runtime_open_jdk_image_version=11.0.23.0

# Pull the UBI 11 docker base image based on provided version or default
FROM docker-na-public.artifactory.swg-devops.com/sec-guardium-next-gen-docker-local/ibm/ibm-semeru-runtime-open-jdk-11-ubi-minimal:${ibm_semeru_runtime_open_jdk_image_version}

ENV ETC=/etc \
    LOGSTASH_VERSION=logstash-oss-8.13.4-linux-x86_64 \
    LOGSTASH_GIT_TAG=v8.13.4 \
    JRUBY_VERSION=9.4.5.0 \
    LOGSTASH_DIR=/usr/share/logstash \
    LOGSTASH_SRC_DIR=/usr/share/logstashSRC \
    LOG_DIR=/var/log/ \
    LOG_GUC_DIR=/var/log/uc/

USER root

#Install necessary packages
RUN microdnf install -y git tzdata openssl curl ca-certificates procps shadow-utils.x86_64 tar gzip hostname vim cronie logrotate sudo findutils && \
    microdnf update -y; microdnf clean all && \
#Change ownership to guc
    groupadd -g 991 guardium && useradd -G guardium -u 1001 guc && \
    mkdir -p ${LOGSTASH_DIR} ${LOG_GUC_DIR} ${LOGSTASH_SRC_DIR} && chown -R guc:guardium ${LOGSTASH_DIR} ${LOG_DIR} ${LOGSTASH_SRC_DIR}
RUN cd ${LOGSTASH_SRC_DIR} && git clone -b "${LOGSTASH_GIT_TAG}" https://github.com/elastic/logstash.git
RUN cd ${LOGSTASH_SRC_DIR}/logstash && ./gradlew --version && ./gradlew clean --warning-mode all && ./gradlew assemble
#Jruby
RUN curl https://repo1.maven.org/maven2/org/jruby/jruby-dist/${JRUBY_VERSION}/jruby-dist-${JRUBY_VERSION}-bin.tar.gz -o jruby-dist-${JRUBY_VERSION}-bin.tar.gz && \
    tar -xzf jruby-dist-${JRUBY_VERSION}-bin.tar.gz && rm -rf jruby-dist-${JRUBY_VERSION}-bin.tar.gz && \
    chown -R guc:guardium jruby-${JRUBY_VERSION}
ENV PATH=$PATH:/jruby-${JRUBY_VERSION}/bin

#Change User to guc
USER guc
#Download and extract logstash
RUN curl https://artifacts.elastic.co/downloads/logstash/${LOGSTASH_VERSION}.tar.gz -o ${LOGSTASH_DIR}/${LOGSTASH_VERSION}.tar.gz && \
    tar -xzf ${LOGSTASH_DIR}/${LOGSTASH_VERSION}.tar.gz  -C ${LOGSTASH_DIR} --strip-components=1 && \
	rm ${LOGSTASH_DIR}/*.tar.gz && \
#Remove UC plugins that were already installed in UC
    cd ${LOGSTASH_DIR} && rm -f *.gem *.zip && \
# Remove unnecessary heavy plugins
    cd ${LOGSTASH_DIR}/vendor/bundle/jruby/*.*.*/gems && \
    rm -rf logstash-filter-geoip-* logstash-input-snmp-* logstash-input-http-* \
    backports-* snappy-* mail-* logstash-filter-jdbc_static-* snmp-* logstash-filter-useragent-* \
    logstash-output-elasticsearch-* logstash-output-s3-* logstash-output-cloudwatch-* logstash-output-http-* logstash-output-webhdfs-* && \
# Clear cache
    rm -rf ${LOGSTASH_DIR}/vendor/bundle/jruby/*.*.*/cache/*.gem
