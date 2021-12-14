# ***************************************************************** */
#                                                                   */
# IBM Confidential                                                  */
#                                                                   */
# OCO Source Materials                                              */
#                                                                   */
# 5737-L66                                                          */
#                                                                   */
# Copyright IBM Corp. 2019, 2021                                    */
#                                                                   */
# The source code for this program is not published or otherwise    */
# divested of its trade secrets, irrespective of what has been      */
# deposited with the U.S. Copyright Office.                         */
#                                                                   */
# ***************************************************************** */

# docker image version
ARG adoptopenjdk_image_version=jre-11.0.13_8_openj9-0.29.0-ubi-minimal-8.4-212

# Pull the UBI 8 docker base image based on provided version or default
FROM sec-guardium-next-gen-docker-local.artifactory.swg-devops.com/openjdk-ubi-minimal:${adoptopenjdk_image_version}

ENV ETC=/etc \
	JAVA_HOME=/opt/java/openjdk \
    PATH="/opt/java/openjdk/bin:${PATH}" \
    LS_JAVA_OPTS="-XX:+UseContainerSupport -Xshareclasses:nonfatal -Dls.cgroup.cpuacct.path.override=/ -Dls.cgroup.cpu.path.override=/" \
	LOGSTASH_VERSION=logstash-oss-7.16.1-linux-x86_64 \
    LOGSTASH_DIR=/usr/share/logstash \
    LOG_DIR=/var/log/ \
    LOG_GUC_DIR=/var/log/uc/ \
    SSL_DIR=/service/certs/universalconnector/

ENV UC_ETC=${LOGSTASH_DIR}/config/ \
    UC_SCRIPTS=${LOGSTASH_DIR}/scripts/ \
    THIRD_PARTY_PATH=${LOGSTASH_DIR}/third_party \
    OUA_BINARY_PATH=${LOGSTASH_DIR}/customer/binaries/oracle_unified_audit_oci_standalone

COPY uc/. /uc/.
COPY health/liveness.sh health/readiness.sh /usr/bin/


#Install necessary packages (libaio required for optional Oracle instant client installation)
RUN microdnf install -y tzdata openssl curl ca-certificates procps shadow-utils.x86_64 tar zip gzip hostname vim cronie logrotate sudo libaio && \
    microdnf update -y; microdnf clean all && \
#Add guc user and prepare
    useradd -g root guc && \
    mkdir -p ${LOGSTASH_DIR} ${LOG_GUC_DIR} ${SSL_DIR} && \
    chmod -vR g=u ${LOGSTASH_DIR} ${LOG_GUC_DIR} ${SSL_DIR} /tmp && \
    chmod +x /usr/bin/readiness.sh /usr/bin/liveness.sh && \
    chown -R guc:root /uc

#Change User to guc
USER guc

#Create a persistent volume which can be monted on the host
VOLUME /var/log/uc
VOLUME /etc/uc/conf


WORKDIR ${LOGSTASH_DIR}

#Download and extract logstash
RUN curl https://artifacts.elastic.co/downloads/logstash/${LOGSTASH_VERSION}.tar.gz -o ${LOGSTASH_DIR}/${LOGSTASH_VERSION}.tar.gz && \
    tar -xzf ${LOGSTASH_DIR}/logstash*.tar.gz  -C ${LOGSTASH_DIR} --strip-components=1 && \
	rm ${LOGSTASH_DIR}/*.tar.gz && \
# Add missing files to LOGSTASH directory
    cp -r /uc/. ${LOGSTASH_DIR}/ && rm -rf /uc/* && \
    echo "--add-opens=java.base/java.lang=ALL-UNNAMED \
    --add-opens=java.base/java.security=ALL-UNNAMED \
    --add-opens=java.base/java.util=ALL-UNNAMED \
    --add-opens=java.base/java.security.cert=ALL-UNNAMED \
    --add-opens=java.base/java.util.zip=ALL-UNNAMED \
    --add-opens=java.base/java.lang.reflect=ALL-UNNAMED \
    --add-opens=java.base/java.util.regex=ALL-UNNAMED \
    --add-opens=java.base/java.net=ALL-UNNAMED \
    --add-opens=java.base/java.io=ALL-UNNAMED \
    --add-opens=java.base/java.lang=ALL-UNNAMED \
    --add-opens=java.base/javax.crypto=ALL-UNNAMED \
    --add-opens=java.management/sun.management=ALL-UNNAMED" >>${UC_ETC}/jvm.options && \
#Install plugins FIXME: enter output_to_guardium plugin to the default offline-package
    ${LOGSTASH_DIR}/bin/logstash-plugin install ${UC_ETC}/logstash-output-java_output_to_guardium-1.3.8.gem && \
    ${LOGSTASH_DIR}/bin/logstash-plugin install file:///${UC_ETC}/guardium_logstash-offline-plugins.zip && \
#Make sure user can read,write and execute the scripts
    chmod 755 ${UC_SCRIPTS}/*.sh && \
#Make sure scripts are not uploaded with windows characters additions
    sed -i -e 's/\r$//' ${LOGSTASH_DIR}/scripts/*.sh ${UC_ETC}/log4j2uc.properties && \
#Grant permissions to group
    chmod -R g=u ${LOGSTASH_DIR} ${LOG_GUC_DIR} ${SSL_DIR} /tmp /opt/java || true && \
#Remove unnecessary heavy plugins
    cd ${LOGSTASH_DIR}/vendor/bundle/jruby/2.5.0/gems && \
    rm -rf logstash-filter-geoip-* logstash-input-snmp-* logstash-input-azure-* logstash-integration-kafka-* logstash-input-http-* nokogiri-* \
    backports-* tzinfo-data-* snappy-* mail-* logstash-input-azure_event_hubs-* logstash-filter-jdbc_static-* snmp-* logstash-filter-useragent-* && \
# Clear cache
    rm -rf ${LOGSTASH_DIR}/vendor/cache/*.gem ${LOGSTASH_DIR}/vendor/bundle/jruby/2.5.0/cache/*.gem && \
# Resolve CVE cve-2021-44228 FIXME: remove this line after upgrading to Logstash 7.16.2 or above
    zip -q -d ${LOGSTASH_DIR}/logstash-core/lib/jars/log4j-core-2.* org/apache/logging/log4j/core/lookup/JndiLookup.class && \
    echo "-Dlog4j2.formatMsgNoLookups=true" >> ${UC_ETC}/jvm.options

ENV PATH ${LOGSTASH_DIR}/bin:$PATH

CMD ${UC_SCRIPTS}/start_logstash.sh