FROM universal-connector-base

#Copy Universal-Connector files
#Note that using 'COPY --chown="guc:guardium" uc/. ${LOGSTASH_DIR}/' command instead of the following 4 commands can save approximately 190MB
USER root
COPY uc/. ${LOGSTASH_DIR}/
RUN chown -R guc:guardium ${LOGSTASH_DIR}
USER guc

ENV UDS_ETC=${LOGSTASH_DIR}/config/ \
    UC_ETC=${LOGSTASH_DIR}/config/ \
    UC_SCRIPTS=${LOGSTASH_DIR}/scripts/ \
    LOG_GUC_DIR=/var/log/uc/

#Create a persistent volume which can be monted on the host
VOLUME /var/log/uc

WORKDIR ${LOGSTASH_DIR}

#Install plugins
RUN cd ${LOGSTASH_DIR}/config/ && \
	${LOGSTASH_DIR}/bin/logstash-plugin install logstash-filter-java_filter_example-0.5.1.gem \
	logstash-output-java_output_to_guardium-1.2.2.gem \
    logstash-filter-mysql_filter_guardium-1.0.0.gem \
    logstash-input-cloudwatch_logs-1.0.3.gem \
    logstash-filter-logstash_filter_s3_guardium-0.5.1.gem && \
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