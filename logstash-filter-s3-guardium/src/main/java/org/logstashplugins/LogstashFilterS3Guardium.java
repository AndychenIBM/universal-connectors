package org.logstashplugins;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.Filter;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.LogstashPlugin;
import co.elastic.logstash.api.PluginConfigSpec;
import com.google.gson.*;
import com.ibm.guardium.s3.Parser;
import com.ibm.guardium.universalconnector.common.structures.Record;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

// class name must match plugin name
@LogstashPlugin(name = "logstash_filter_s3_guardium")
public class LogstashFilterS3Guardium implements Filter {

    private static Log log = LogFactory.getLog(LogstashFilterS3Guardium.class);

    public static final PluginConfigSpec<String> SOURCE_CONFIG = PluginConfigSpec.stringSetting("source", "message");
    public static final String LOGSTASH_TAG_S3_JSON_PARSE_ERROR = "_s3_json_parse_error";



    private String id;
    private String sourceField; 
    private final static String MONGOAUDIT_START_SIGNAL = "mongod: ";
    private Gson gson;

    public LogstashFilterS3Guardium(String id, Configuration config, Context context) {
        // constructors should validate configuration options
        // init log properties
        String uc_etc = System.getenv("UC_ETC");
        try{
            PropertyConfigurator.configureAndWatch(uc_etc + "/log4j.properties", 10000);
        } catch (Exception e){
            System.out.println("LogstashFilterS3Guardium - Failed to find log4j file");
            log.error("Failed to log4j file");
            //throw new IllegalArgumentException("Filed to find files during connector initialization, Path base is "+uc_etc, e);
        }
        log.debug("Finished JavaOutputToGuardium constructor");
        this.id = id;
        if (config != null) {
            this.sourceField = config.get(SOURCE_CONFIG);
        }
        final GsonBuilder builder = new GsonBuilder();
        builder.serializeNulls();
        gson = builder.create();

    }

    @Override
    public Collection<Event> filter(Collection<Event> events, FilterMatchListener matchListener) {
        ArrayList<Event> skippedEvents = new ArrayList<>();
        for (Event e : events) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Event: "+logEvent(e));
                }
                JsonElement inputJSON = null;
                if (e.getField("detail") !=null ) {
                    String jsonDetailsEvent = gson.toJson(e.getField("detail"));
                    log.debug("DETAIL AS JSON STR3 " + jsonDetailsEvent);
                    inputJSON = JsonParser.parseString(jsonDetailsEvent);
                } else {
                    inputJSON = gson.toJsonTree(e);
                }

                Record record = Parser.buildRecord(inputJSON);
                if (record==null){
                    log.warn("Failed to parse event "+logEvent(e));
                    continue;
                }

                e.setField("Record", gson.toJson(record));

                matchListener.filterMatched(e); // Flag OK for filter input/parsing/out
                        
            } catch (Exception ex) {
                log.error("Error parsing jsonDetailsEvent  "+logEvent(e), ex);
                e.tag(LOGSTASH_TAG_S3_JSON_PARSE_ERROR);
            }
        }

        // Remove skipped mongodb events from reaching output
        // FIXME log which events skipped
        events.removeAll(skippedEvents);
        return events;
    }

    private static String logEvent(Event event){
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("{ ");
            boolean first = true;
            for (Map.Entry<String, Object> stringObjectEntry : event.getData().entrySet()) {
                if (!first){
                    sb.append(",");
                }
                sb.append("\""+stringObjectEntry.getKey()+"\" : \""+stringObjectEntry.getValue()+"\"");
                first = false;
            }
            sb.append(" }");
            return sb.toString();
        } catch (Exception e){
            log.error("Failed to create event log string", e);
            return null;
        }
    }

    @Override
    public Collection<PluginConfigSpec<?>> configSchema() {
        // should return a list of all configuration options for this plugin
        return Collections.singletonList(SOURCE_CONFIG);
    }

    @Override
    public String getId() {
        return this.id;
    }
}
