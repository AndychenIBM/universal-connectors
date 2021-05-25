FROM sec-guardium-next-gen-docker-local.artifactory.swg-devops.com/universal-connector-base

LABEL maintainer="IBM Security" \
    #build_date=
    #version=
    #release=
    name="Universal-Connector" \
    summary="Image for Guardium Universal Connector" \
    description="This image contains universal-connector-base image, along with plugins for supporting a variety of databses. For more information on this image please see documentation files" \
    vendor="IBM Corp."

ENV UC_ETC=${LOGSTASH_DIR}/config/ \
    UC_SCRIPTS=${LOGSTASH_DIR}/scripts/ \
    THIRD_PARTY_PATH=${LOGSTASH_DIR}/third_party \
    SSL_DIR=/service/certs/universalconnector/

#TODO: consider moving SSL_DIR definition to base-image and use 'COPY --chown="guc:guardium" uc/. ${LOGSTASH_DIR}/' instead of switching users.
USER root
COPY uc/. ${LOGSTASH_DIR}/
RUN mkdir -p ${SSL_DIR} && chown -R guc:guardium ${LOGSTASH_DIR} ${SSL_DIR}
USER guc

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
#Install plugins FIXME: enter output_to_guardium plugin to the default offline-package
    ${LOGSTASH_DIR}/bin/logstash-plugin install ${UC_ETC}/logstash-output-java_output_to_guardium-1.3.3.gem && \
    ${LOGSTASH_DIR}/bin/logstash-plugin install file:///${UC_ETC}/guardium_logstash-offline-pluginsWToutput.zip && \
#Make sure user can read,write and execute the scripts
    chmod 755 ${UC_SCRIPTS}/*.sh && \
#Make sure scripts are not uploaded with windows characters additions
    sed -i -e 's/\r$//' ${LOGSTASH_DIR}/scripts/*.sh ${UC_ETC}/log4j2uc.properties && \
#Clear cache
    rm -rf $LOGSTASH_DIR/vendor/cache/*.gem $LOGSTASH_DIR/vendor/bundle/jruby/2.5.0/cache/*.gem

ENV PATH ${LOGSTASH_DIR}/bin:$PATH

CMD ${UC_SCRIPTS}/start_logstash.sh