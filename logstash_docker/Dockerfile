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
ARG adoptopenjdk_image_version=jre-11.0.11_9_openj9-0.26.0-ubi-minimal-8.4-205.1626828526

# Pull the UBI 8 docker base image based on provided version or default
FROM sec-guardium-next-gen-docker-local.artifactory.swg-devops.com/openjdk-ubi-minimal:${adoptopenjdk_image_version}

ENV ETC=/etc \
	JAVA_HOME=/opt/java/openjdk \
    PATH="/opt/java/openjdk/bin:${PATH}" \
    LS_JAVA_OPTS="-XX:+UseContainerSupport -Xshareclasses:nonfatal" \
	LOGSTASH_VERSION=7.10.0-linux-x86_64 \
    LOGSTASH_DIR=/usr/share/logstash \
    LOG_DIR=/var/log/ \
    LOG_GUC_DIR=/var/log/uc/ \
    SSL_DIR=/service/certs/universalconnector/

ENV UC_ETC=${LOGSTASH_DIR}/config/ \
    UC_SCRIPTS=${LOGSTASH_DIR}/scripts/ \
    THIRD_PARTY_PATH=${LOGSTASH_DIR}/third_party \
    OUA_BINARY_PATH=${LOGSTASH_DIR}/customer/binaries/oracle_unified_audit_oci_standalone

COPY uc/. /uc/.


#Install necessary packages (libaio required for optional Oracle instant client installation)
RUN microdnf install -y tzdata openssl curl ca-certificates procps shadow-utils.x86_64 tar gzip hostname vim cronie logrotate sudo libaio && \
    microdnf update -y; microdnf clean all && \
#Add guc user and prepare
    useradd -g root guc && \
    mkdir -p ${LOGSTASH_DIR} ${LOG_GUC_DIR} ${SSL_DIR} && \
    chmod -vR g=u ${LOGSTASH_DIR} ${LOG_GUC_DIR} ${SSL_DIR} /tmp

USER root
COPY health/liveness.sh /usr/bin
COPY health/readiness.sh /usr/bin
RUN chmod +x /usr/bin/readiness.sh /usr/bin/liveness.sh

#Change User to guc
USER guc

#Create a persistent volume which can be monted on the host
VOLUME /var/log/uc
VOLUME /etc/uc/conf


WORKDIR ${LOGSTASH_DIR}

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
    cp -r /uc/. ${LOGSTASH_DIR}/ && \
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
    ${LOGSTASH_DIR}/bin/logstash-plugin install ${UC_ETC}/logstash-output-java_output_to_guardium-1.3.6.gem && \
    ${LOGSTASH_DIR}/bin/logstash-plugin install file:///${UC_ETC}/guardium_logstash-offline-pluginsWToutput.zip && \
#Make sure user can read,write and execute the scripts
    chmod 755 ${UC_SCRIPTS}/*.sh && \
#Make sure scripts are not uploaded with windows characters additions
    sed -i -e 's/\r$//' ${LOGSTASH_DIR}/scripts/*.sh ${UC_ETC}/log4j2uc.properties && \
#Clear cache
    rm -rf $LOGSTASH_DIR/vendor/cache/*.gem $LOGSTASH_DIR/vendor/bundle/jruby/2.5.0/cache/*.gem && \
#Grant permissions to group
    chmod -R g=u ${LOGSTASH_DIR} ${LOG_GUC_DIR} ${SSL_DIR} /tmp /opt/java || true

ENV PATH ${LOGSTASH_DIR}/bin:$PATH

CMD ${UC_SCRIPTS}/start_logstash.sh