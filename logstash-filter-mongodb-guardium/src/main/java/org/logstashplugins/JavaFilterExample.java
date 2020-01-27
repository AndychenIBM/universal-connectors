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
                int mongodIndex = messageString.indexOf(MONGOAUDIT_START_SIGNAL);
                if (mongodIndex != -1) {
                    String input = messageString.substring(mongodIndex + MONGOAUDIT_START_SIGNAL.length());
                    try {
                        JsonObject inputJSON = (JsonObject) JsonParser.parseString(input);
                        // FIXME move filterMatched later?
                        matchListener.filterMatched(e); // Flag OK for filter input/parsing/out
                        String constructString = Parser.Parse(inputJSON);
                        
                        // TODO: start parsing in another class
                        e.setField("Construct", constructString); 

                    } catch (Exception exception) {
                        // FIXME: Throw? as not proper json or syntax error
                        // don't let event pass filter
                    }
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
