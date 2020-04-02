package org.logstashplugins;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.Filter;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.LogstashPlugin;
import co.elastic.logstash.api.PluginConfigSpec;
import com.google.gson.*;
import com.google.gson.JsonParser;
import com.ibm.guardium.Parser;
import com.ibm.guardium.connector.structures.Record;
import com.ibm.guardium.connector.structures.SessionLocator;

import java.util.Collection;
import java.util.Collections;

// class name must match plugin name
@LogstashPlugin(name = "java_filter_example")
public class JavaFilterExample implements Filter {

    public static final PluginConfigSpec<String> SOURCE_CONFIG =
            PluginConfigSpec.stringSetting("source", "message");

    private String id;
    private String sourceField; 
    private final static String MONGOAUDIT_START_SIGNAL = "mongod: ";  

    public JavaFilterExample(String id, Configuration config, Context context) {
        // constructors should validate configuration options
        this.id = id;
        if (config != null) {
            this.sourceField = config.get(SOURCE_CONFIG);
        }
    }

    @Override
    public Collection<Event> filter(Collection<Event> events, FilterMatchListener matchListener) {
        for (Event e : events) {
            // from config, use Object f = e.getField(sourceField);
            if (e.getField("message") instanceof String) {
                String messageString = e.getField("message").toString();
                //TODO abandon finding "mongod:"; use syslog_message
                int mongodIndex = messageString.indexOf(MONGOAUDIT_START_SIGNAL);
                if (mongodIndex != -1) {
                    String input = messageString.substring(mongodIndex + MONGOAUDIT_START_SIGNAL.length());
                    try {
                        JsonObject inputJSON = (JsonObject) JsonParser.parseString(input);
                        // FIXME move filterMatched later?
                        matchListener.filterMatched(e); // Flag OK for filter input/parsing/out
                        

                        Record record = Parser.parseRecord(inputJSON);

                        // server_hostname
                        if (e.getField("server_hostname") instanceof String) {
                            String serverHost = e.getField("server_hostname").toString();
                            if (serverHost != null)
                                record.getAccessor().setServerHostName(serverHost);
                        }
                        if (e.getField("source_program") instanceof String) {
                            String sourceProgram = e.getField("source_program").toString();
                            if (sourceProgram != null)
                                record.getAccessor().setSourceProgram(sourceProgram);
                        }

                        // override client/server IP, if 127.0.0.1 or "(None)"
                        SessionLocator sessionLocator = record.getSessionLocator();
                        if (sessionLocator.getServerIp().equalsIgnoreCase(sessionLocator.getClientIp())
                                && e.getField("host") instanceof String) {

                            String host = e.getField("host").toString();

                            if (host != null) {
                                sessionLocator.setServerIp(host);
                                sessionLocator.setClientIp(host);
                            }
                        }
                        
                        // TODO: Remove flat variables after Record is used.
                        e.setField("timestamp", Parser.parseTimestamp(inputJSON));
                        
                        final GsonBuilder builder = new GsonBuilder();
                        builder.serializeNulls();
                        final Gson gson = builder.create();
                        e.setField("Record", gson.toJson(record));

                    } catch (Exception exception) {
                        // FIXME: Throw? as not proper json or syntax error
                        // don't let event pass filter
                        // TODO log event removed? 
                        //events.remove(e);
                        e.tag("_mongoguardium_json_parse_error");
                    }
                } else {
                    //events.remove(e);
                    e.tag("_mongoguardium_skip");
                }
            }
        }
        return events;
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
