# ***************************************************************** */
#                                                                   */
# IBM Confidential                                                  */
#                                                                   */
# OCO Source Materials                                              */
#                                                                   */
# 5737-L66                                                          */
#                                                                   */
# Copyright IBM Corp. 2019, 2023                                    */
#                                                                   */
# The source code for this program is not published or otherwise    */
# divested of its trade secrets, irrespective of what has been      */
# deposited with the U.S. Copyright Office.                         */
#                                                                   */
# ***************************************************************** */

# docker image version
ARG ibm_semeru_runtimes_ubi_minimal_image_version=open-11.0.16.1_1-jre-focal_ubi-minimal-8.7-1085

# Pull the UBI 8 docker base image based on provided version or default
FROM docker-na-public.artifactory.swg-devops.com/sec-guardium-next-gen-docker-local/ibm-semeru-runtimes-ubi-minimal:${ibm_semeru_runtimes_ubi_minimal_image_version}

ENV ETC=/etc \
	JAVA_HOME=/opt/java/openjdk \
    PATH="/opt/java/openjdk/bin:${PATH}" \
    LS_JAVA_OPTS="-XX:+UseContainerSupport -Xshareclasses:nonfatal -Dls.cgroup.cpuacct.path.override=/ -Dls.cgroup.cpu.path.override=/" \
	LOGSTASH_VERSION=logstash-oss-7.17.6-linux-x86_64 \
    LOGSTASH_DIR=/usr/share/logstash \
    LOG_DIR=/var/log/ \
    LOG_GUC_DIR=/var/log/uc/ \
    CERTS_PATH=/service/certs/ \
    SSL_DIR=/service/certs/universalconnector/ \
    CLIENT_CERTS_PATH=/service/certs/client/ \
    RPM_DIR=/var/lib/rpm

ENV UC_ETC=${LOGSTASH_DIR}/config/ \
    UC_SCRIPTS=${LOGSTASH_DIR}/scripts/ \
    THIRD_PARTY_PATH=${LOGSTASH_DIR}/third_party \
    OUA_BINARY_PATH=${LOGSTASH_DIR}/customer/binaries/oracle_unified_audit_oci_standalone \
    LS_JAVA_HOME=${JAVA_HOME}

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
# Install plugins
# TODO: uncomment after v1.2.2 is available (can be applied to Logstash 8.2 or above)
#    ${LOGSTASH_DIR}/bin/logstash-plugin install --version '1.2.2' logstash-input-google_pubsub && \
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
    cd ${LOGSTASH_DIR}/vendor/bundle/jruby/2.5.0/gems && \
    rm -rf logstash-filter-geoip-* logstash-input-snmp-* logstash-input-http-* nokogiri-* \
    backports-* tzinfo-data-* snappy-* mail-* logstash-filter-jdbc_static-* snmp-* logstash-filter-useragent-* \
    logstash-output-elasticsearch-* logstash-output-s3-* logstash-output-cloudwatch-* logstash-output-http-* logstash-output-webhdfs-* && \
# Clear cache
    rm -rf ${LOGSTASH_DIR}/vendor/bundle/jruby/2.5.0/cache/*.gem

ENV PATH ${LOGSTASH_DIR}/bin:$PATH

CMD ${UC_SCRIPTS}/start_logstash.sh
