# ***************************************************************** */
#                                                                   */
# IBM Confidential                                                  */
#                                                                   */
# OCO Source Materials                                              */
#                                                                   */
# 5737-L66                                                          */
#                                                                   */
# Copyright IBM Corp. 2019, 2022                                    */
#                                                                   */
# The source code for this program is not published or otherwise    */
# divested of its trade secrets, irrespective of what has been      */
# deposited with the U.S. Copyright Office.                         */
#                                                                   */
# ***************************************************************** */

# docker image version
ARG ibm_semeru_runtimes_ubi_minimal_image_version=open-11.0.14.1_1-jre-focal_ubi-minimal-8.5-230.1645809059

# Pull the UBI 8 docker base image based on provided version or default
FROM sec-guardium-next-gen-docker-local.artifactory.swg-devops.com/ibm-semeru-runtimes-ubi-minimal:${ibm_semeru_runtimes_ubi_minimal_image_version}

ENV ETC=/etc \
	JAVA_HOME=/opt/java/openjdk \
    PATH="/opt/java/openjdk/bin:${PATH}" \
    JAVA_OPTS="-XX:+UseContainerSupport" \
	LOGSTASH_VERSION=7.5.2 \
    LOGSTASH_DIR=/usr/share/logstash \
    LOG_DIR=/var/log/ \
    LOG_GUC_DIR=/var/log/uc/ \
    LOGSTASH_INPUT_BEATS_VERSION=6.0.11 \
    LOGSTASH_INPUT_TCP_VERSION=6.0.6
	
USER root

#Install necessary packages
RUN microdnf install -y tzdata openssl curl ca-certificates procps shadow-utils.x86_64 tar gzip hostname vim cronie logrotate sudo && \
    microdnf update -y; microdnf clean all && \
#Change ownership to guc
    groupadd -g 991 guardium && useradd -G guardium -u 1001 guc && \
    mkdir -p ${LOGSTASH_DIR} ${LOG_GUC_DIR} && chown -R guc:guardium ${LOGSTASH_DIR} ${LOG_DIR}
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
    mail-2.6.6 logstash-input-azure_event_hubs-1.1.2 logstash-filter-jdbc_static-1.0.7 snmp-1.3.2 && \
    ${LOGSTASH_DIR}/bin/logstash-plugin install --version ${LOGSTASH_INPUT_BEATS_VERSION} logstash-input-beats && \
    ${LOGSTASH_DIR}/bin/logstash-plugin update logstash-codec-plain&& \
    ${LOGSTASH_DIR}/bin/logstash-plugin update logstash-input-tcp
