FROM sec-guardium-next-gen-docker-local.artifactory.swg-devops.com/universal-connector-base

LABEL maintainer="IBM Security" \
    #build_date=
    #version=
    #release=
    name="Universal-Connector" \
    summary="Image for Guardium Universal Connector" \
    description="This image contains universal-connector-base image, along with plugins for supporting a variety of databses. For more information on this image please see documentation files" \
    vendor="IBM Corp."

#Copy Universal-Connector files
#Note that using 'COPY --chown="guc:guardium" uc/. ${LOGSTASH_DIR}/' command instead of the following 4 commands can save approximately 190MB
USER root
COPY uc/. ${LOGSTASH_DIR}/
RUN chown -R guc:guardium ${LOGSTASH_DIR}
USER guc

ENV UC_ETC=${LOGSTASH_DIR}/config/ \
    UC_SCRIPTS=${LOGSTASH_DIR}/scripts/ \
    THIRD_PARTY_PATH=${LOGSTASH_DIR}/third_party \
    SSL_DIR=${LOGSTASH_DIR}/ssl

#Create a persistent volume which can be monted on the host
VOLUME /var/log/uc
VOLUME /etc/uc/conf

WORKDIR ${LOGSTASH_DIR}

RUN echo "--add-opens=java.base/java.lang=ALL-UNNAMED \
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
#Install plugins
    cd ${LOGSTASH_DIR}/config/ && \
	${LOGSTASH_DIR}/bin/logstash-plugin install logstash-filter-mongodb_guardium_filter-0.6.2.gem \
	logstash-output-java_output_to_guardium-1.2.8.gem \
    logstash-filter-mysql_filter_guardium-1.0.0.gem \
    logstash-input-cloudwatch_logs-1.0.3.gem \
    logstash-filter-logstash_filter_s3_guardium-0.5.3.gem && \
#Create a customer dir
    mkdir -p ${LOGSTASH_DIR}/customer/pipeline ${LOGSTASH_DIR}/customer/config && \
#Make sure user can read,write and execute the scripts
    chmod 755 ${UC_SCRIPTS}/*.sh && \
#Make sure scripts are not uploaded with windows characters additions
    sed -i -e 's/\r$//' ${LOGSTASH_DIR}/scripts/*.sh ${UC_ETC}/log4j2uc.properties && \
#Clear cache
    rm -rf $LOGSTASH_DIR/vendor/cache/*.gem $LOGSTASH_DIR/vendor/bundle/jruby/2.5.0/cache/*.gem

ENV PATH ${LOGSTASH_DIR}/bin:$PATH

CMD ${UC_SCRIPTS}/start_logstash.sh