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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

// class name must match plugin name
@LogstashPlugin(name = "java_filter_example")
public class JavaFilterExample implements Filter {

    public static final PluginConfigSpec<String> SOURCE_CONFIG =
            PluginConfigSpec.stringSetting("source", "message");
    public static final String LOGSTASH_TAG_SKIP_NOT_MONGODB = "_mongoguardium_skip_not_mongodb"; // skip messages that do not contain "mongod:"
    /* skipping non-mongo syslog messages, and non-relevant log events 
        like "createUser", "createCollection", ... as these are already parsed in prior authCheck messages. */ 
    public static final String LOGSTASH_TAG_SKIP = "_mongoguardium_skip"; 
    public static final String LOGSTASH_TAG_JSON_PARSE_ERROR = "_mongoguardium_json_parse_error";

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
        ArrayList<Event> skippedEvents = new ArrayList<>();
        for (Event e : events) {
            // from config, use Object f = e.getField(sourceField);
            if (e.getField("message") instanceof String) {
                String messageString = e.getField("message").toString();
                // finding "mongod:" to be general (syslog, filebeat)
                // alternatively, throw JSON audit part into a specific field
                int mongodIndex = messageString.indexOf(MONGOAUDIT_START_SIGNAL);
                if (mongodIndex != -1) {
                    String input = messageString.substring(mongodIndex + MONGOAUDIT_START_SIGNAL.length());
                    try {
                        JsonObject inputJSON = (JsonObject) JsonParser.parseString(input);
                        
                        // filter events
                        final String atype = inputJSON.get("atype").getAsString();
                        if ((!atype.equals("authCheck") && !atype.equals("authenticate")) 
                            || (atype.equals("authenticate") && inputJSON.get("result").getAsString().equals("0"))) {
                            e.tag(LOGSTASH_TAG_SKIP);
							skippedEvents.add(e);
                            continue;
                        }
                        

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

                        // Override "(NONE)" IP, if not filterd, as it's internal command by MongoDB.
                        // Note: IP needs to be in ipv4/ipv6 format
                        
                        SessionLocator sessionLocator = record.getSessionLocator();
                        if (sessionLocator.getServerIp().equalsIgnoreCase("(NONE)")) {
                            sessionLocator.setServerIp("0.0.0.0");
                        }
                        if (sessionLocator.getClientIp().equalsIgnoreCase("(NONE)")) {
                            sessionLocator.setClientIp("0.0.0.0");
                        }

                        // TODO: Remove flat variables after Record is used.
                        e.setField("timestamp", Parser.parseTimestamp(inputJSON));
                        
                        final GsonBuilder builder = new GsonBuilder();
                        builder.serializeNulls();
                        final Gson gson = builder.create();
                        e.setField("Record", gson.toJson(record));

                        matchListener.filterMatched(e); // Flag OK for filter input/parsing/out
                        
                    } catch (Exception exception) {
                        // FIXME: Throw? as not proper json or syntax error
                        // don't let event pass filter
                        // TODO log event removed? 
                        //events.remove(e);
                        e.tag(LOGSTASH_TAG_JSON_PARSE_ERROR);
                    }
                } else {
                    e.tag(LOGSTASH_TAG_SKIP_NOT_MONGODB);
                }
            }
        }

        // Remove skipped mongodb events from reaching output
        // FIXME log which events skipped
        events.removeAll(skippedEvents);
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
