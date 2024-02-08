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
ARG rhel_ubi_minimal_image_version=8.9-1108

# Pull the UBI 8 docker base image based on provided version or default
FROM registry.access.redhat.com/ubi8/ubi-minimal:${rhel_ubi_minimal_image_version}

ENV ETC=/etc \

    JAVA_HOME="/usr/share/logstash/jdk" \
    PATH="${JAVA_HOME}/bin:${PATH}" \
	LOGSTASH_VERSION=logstash-oss-8.12.0-linux-x86_64 \
    LOGSTASH_DIR=/usr/share/logstash \
    LOG_DIR=/var/log/ \
    LOG_GUC_DIR=/var/log/uc/ \
    CERTS_PATH=/service/certs/ \
    SSL_DIR=/service/certs/universalconnector/ \
    CLIENT_CERTS_PATH=/service/certs/client/ \
    RPM_DIR=/var/lib/rpm \
    UC_OPENSOURCE_TAG=v1.5.2 \
    UC_OPENSOURCE_OFFLINE_PACKAGE=uc_gi_offline_package.zip

ENV UC_ETC=${LOGSTASH_DIR}/config/ \
    UC_SCRIPTS=${LOGSTASH_DIR}/scripts/ \
    THIRD_PARTY_PATH=${LOGSTASH_DIR}/third_party \
    OUA_BINARY_PATH=${LOGSTASH_DIR}/customer/binaries/oracle_unified_audit_oci_standalone

ENV TROUBLESHOOT_OUTPUT=${UC_SCRIPTS}/troubleshooting_output.txt \
    TROUBLESHOOTING="OK"

COPY uc/. /uc/.
COPY health/liveness.sh health/readiness.sh /usr/bin/


# Install necessary packages (libaio required for optional Oracle instant client installation)
RUN microdnf install -y tzdata openssl curl ca-certificates procps shadow-utils.x86_64 tar zip gzip hostname vim cronie logrotate sudo libaio && \
    microdnf update -y; microdnf clean all && \
# Add guc user and prepare
    useradd -g root -u 1001 guc && \
    mkdir -p ${LOGSTASH_DIR} ${LOG_GUC_DIR} ${SSL_DIR} ${CLIENT_CERTS_PATH} && \
    chmod -Rf g=u ${LOGSTASH_DIR} ${LOG_GUC_DIR} ${SSL_DIR} ${CLIENT_CERTS_PATH} ${RPM_DIR} /tmp && \
    chmod +x /usr/bin/readiness.sh /usr/bin/liveness.sh && \
    chown -R guc:root /uc

# Change User to guc
USER guc

# Create a persistent volume which can be monted on the host
VOLUME /var/log/uc
VOLUME /etc/uc/conf

WORKDIR ${LOGSTASH_DIR}

# Download and extract logstash
RUN curl https://artifacts.elastic.co/downloads/logstash/${LOGSTASH_VERSION}.tar.gz -o ${LOGSTASH_DIR}/${LOGSTASH_VERSION}.tar.gz && \
    tar -xzf ${LOGSTASH_DIR}/logstash*.tar.gz  -C ${LOGSTASH_DIR} --strip-components=1 && \
	rm ${LOGSTASH_DIR}/*.tar.gz && \
# Add missing files to LOGSTASH directory
    cp -r /uc/. ${LOGSTASH_DIR}/ && rm -rf /uc/* && \
    echo -e "\n--add-opens=java.base/java.lang=ALL-UNNAMED \n\
--add-opens=java.base/java.security=ALL-UNNAMED \n\
--add-opens=java.base/java.util=ALL-UNNAMED \n\
--add-opens=java.base/java.security.cert=ALL-UNNAMED \n\
--add-opens=java.base/java.util.zip=ALL-UNNAMED \n\
--add-opens=java.base/java.lang.reflect=ALL-UNNAMED \n\
--add-opens=java.base/java.util.regex=ALL-UNNAMED \n\
--add-opens=java.base/java.net=ALL-UNNAMED \n\
--add-opens=java.base/java.io=ALL-UNNAMED \n\
--add-opens=java.base/java.nio=ALL-UNNAMED \n\
--add-opens=java.base/java.lang=ALL-UNNAMED \n\
--add-opens=java.base/javax.crypto=ALL-UNNAMED \n\
--add-opens=java.management/sun.management=ALL-UNNAMED \n\
--add-exports=java.base/sun.net.util=ALL-UNNAMED" >>${UC_ETC}/jvm.options && \
# Install plugins from opensource repo
    curl -L https://github.com/IBM/universal-connectors/releases/download/${UC_OPENSOURCE_TAG}/${UC_OPENSOURCE_OFFLINE_PACKAGE} -o ${UC_ETC}/${UC_OPENSOURCE_OFFLINE_PACKAGE} && \
    ${LOGSTASH_DIR}/bin/logstash-plugin install file:///${UC_ETC}/${UC_OPENSOURCE_OFFLINE_PACKAGE} && \
# Install internal plugins
    ${LOGSTASH_DIR}/bin/logstash-plugin install file:///${UC_ETC}/guardium_logstash-offline-plugins.zip && \
    ${LOGSTASH_DIR}/bin/logstash-plugin install file:///${UC_ETC}/logstash-filter-json_encode-3.0.3-java.zip && \
# Make sure user can read,write and execute the scripts
    chmod -f 755 ${UC_SCRIPTS}/*.sh && \
# Make sure scripts are not uploaded with windows characters additions
    sed -i -e 's/\r$//' ${LOGSTASH_DIR}/scripts/*.sh ${UC_ETC}/log4j2uc.properties && \
# Grant permissions to group
    chmod -Rf g=u ${LOGSTASH_DIR} ${LOG_GUC_DIR} ${SSL_DIR} /tmp /opt/java || true && \
# Remove UC plugins that were already installed in UC
    cd ${UC_ETC} && rm -f *.gem *.zip && \
# Remove unnecessary heavy plugins
    cd ${LOGSTASH_DIR}/vendor/bundle/jruby/*.*.*/gems && \
    rm -rf logstash-filter-geoip-* logstash-input-snmp-* logstash-input-http-* \
    backports-* snappy-* mail-* logstash-filter-jdbc_static-* snmp-* logstash-filter-useragent-* \
    logstash-output-elasticsearch-* logstash-output-s3-* logstash-output-cloudwatch-* logstash-output-http-* logstash-output-webhdfs-* && \
# Clear cache
    rm -rf ${LOGSTASH_DIR}/vendor/bundle/jruby/*.*.*/cache/*.gem

ENV PATH="${LOGSTASH_DIR}/bin:${JAVA_HOME}/bin:${PATH}" \
    LS_JAVA_OPTS="-Dls.cgroup.cpuacct.path.override=/ -Dls.cgroup.cpu.path.override=/ $LS_JAVA_OPTS"

CMD ${UC_SCRIPTS}/start_logstash.sh
