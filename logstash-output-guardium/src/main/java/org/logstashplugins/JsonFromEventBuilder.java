package org.logstashplugins;

import co.elastic.logstash.api.Event;
import com.google.gson.Gson;
import com.ibm.guardium.universalconnector.transformer.jsonrecord.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class JsonFromEventBuilder {
    private static Log log = LogFactory.getLog(JsonFromEventBuilder.class);

    private static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private static SimpleDateFormat dateFormattter = new SimpleDateFormat(DATE_FORMAT);

    public Record buildRecord(Event event) throws ParseException {
        Record record = new Record();

        record.setDbName(getStringField(event, "dbName", "schema"));
        record.setAppUserName(getStringField(event, "app_user_name", "no_app_user"));
        record.setSessionId(getStringField(event, "session_id", "sessionId123456789"));
        record.setTime(getTimeInSec(event));

        SessionLocator session = buildSession(event);
        record.setSessionLocator(session);

        Accessor accessor = buildAccessor(event);
        record.setAccessor(accessor);

        Data data = buildData(event);
        record.setData(data);

        return record;
    }


    public SessionLocator buildSession(Event event){
        SessionLocator session = new SessionLocator();
        session.setClientIp(getStringField(event, "client_ip", "127.0.0.1"));
        session.setClientIpv6(getStringField(event, "client_ip_v6", ""));
        session.setClientPort(getIntField(event, "client_port", 0));
        session.setIpv6(false);
        session.setServerIp(getStringField(event, "server_ip", "9.70.147.59"));
        session.setServerIpv6(getStringField(event, "server_ip_v6", ""));
        session.setServerPort(getIntField(event, "server_port", 0));
        return session;
    }

    public Accessor buildAccessor(Event event){
        Accessor accessor = new Accessor();
        accessor.setClient_mac(getStringField(event, "mac_address", "00:00:00:a1:2b:cc"));
        accessor.setClientHostName(getStringField(event, "client_host_name", "localhost"));
        accessor.setClientOs(getStringField(event, "os", "os_not_found"));
        accessor.setCommProtocol(getStringField(event, "communication_protocol", "shell"));
        accessor.setDbProtocol(getStringField(event, "db_protocol", "MONGODB"));
        accessor.setDbProtocolVersion(getStringField(event, "protocol_version", "0"));
        accessor.setDbUser(getStringField(event, "db_user", "not_found"));
        accessor.setLanguage(getStringField(event, "language", "MONGODB" ));
        accessor.setOsUser(getStringField(event, "os_user", "root"));
        accessor.setServerType(getStringField(event, "server_type", "IDIDNOTFOUNDIT"));
        accessor.setServerDescription(getStringField(event, "server_desc", "NA"));
        accessor.setServerOs(getStringField(event, "server_os", "noserverod"));
        accessor.setServerHostName(getStringField(event, "server_hostname", "noserverhost"));
        accessor.setServiceName(getStringField(event, "service_name", "noservice"));
        accessor.setSourceProgram(getStringField(event, "source_program", "nosourceprogram"));
        accessor.setType(getStringField(event, "type", "notype"));
        return accessor;
    }


    public Data buildData(Event e) {
        Data data = new Data();
        data.setOriginalSqlCommand(getStringField(e, "original_sql", "nosql"));
        data.setOriginalSqlCommand(getStringField(e, "original_sql", "nosql"));
        try {
            data.setTimestamp(getTimeInSec(e));
            String  constructStr = getStringField(e, "Construct", null);
            if (constructStr!=null) {
                Construct construct = (new Gson()).fromJson(constructStr, Construct.class);
                data.setConstruct(construct);
                data.setUseConstruct(true);
                //todo: remove once Tal updates filter
                if (construct.getFull_sql()==null){
                    construct.setFull_sql("nofullsql");
                }
                if (construct.getOriginal_sql()==null){
                    construct.setOriginal_sql("nooriginalsql");
                }
            }
        } catch (Exception ex){
            log.error("Failed to parse construct, putting empty content for it ", ex);
            System.out.print("No construct found");
            data.setUseConstruct(false);
        }
        return data;
    }

    private String getStringField(Event e, String s, String defaultValue) {
        return e.getField(s) == null ? defaultValue : e.getField(s).toString();
    }

    private int getIntField(Event e, String s, int defaultValue) {
        return e.getField(s) == null ? defaultValue : Integer.parseInt(e.getField(s).toString());
    }

    private int getTimeInSec(Event event) throws ParseException {
        String dateStr = getStringField(event, "timestamp", null);
        if (dateStr==null) {
            dateStr = getStringField(event, "@timestamp", "2020-02-12T07:30:25.092-0500");
        }
        Date date = dateFormattter.parse(dateStr);
        int timeInMs = (int)(date.getTime() / 1000); //todo: check this
        return timeInMs;
    }
}
