package org.logstashplugins;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.Filter;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.LogstashPlugin;
import co.elastic.logstash.api.PluginConfigSpec;
import com.google.gson.*;
import com.ibm.guardium.Parser;
import com.ibm.guardium.universalconnector.common.Util;
import com.ibm.guardium.universalconnector.common.structures.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.util.*;

// class name must match plugin name
@LogstashPlugin(name = "java_filter_example")
public class JavaFilterExample implements Filter {

    public static final String LOG42_CONF="log4j2uc.properties";
    static {
        try {
            String uc_etc = System.getenv("UC_ETC");
            LoggerContext context = (LoggerContext) LogManager.getContext(false);
            File file = new File(uc_etc +File.pathSeparator+LOG42_CONF);
            context.setConfigLocation(file.toURI());
        } catch (Exception e){
            System.err.println("Failed to load log4j configuration "+e.getMessage());
            e.printStackTrace();
        }
    }
    private static Log log = LogFactory.getLog(JavaFilterExample.class);

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
    private final static Set<String> LOCAL_IP_LIST = new HashSet<>(
        Arrays.asList("127.0.0.1", "0:0:0:0:0:0:0:1"));

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
                        
                        // filter internal and not parsed events
                        final String atype = inputJSON.get("atype").getAsString();
                        final JsonArray users = inputJSON.getAsJsonArray("users");
                        if ((!atype.equals("authCheck") && !atype.equals("authenticate")) // filter handles only authCheck message template & authentication error,
                            || (atype.equals("authenticate") && inputJSON.get("result").getAsString().equals("0")) // not auth success,
                            || (users.size() == 0 && !atype.equals("authenticate")) )  { // nor messages with empty users array, as it's an internal command (except authenticate, which states in param.user)
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

                        this.correctIPs(e, record);

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
                        log.error("Error parsing mongo event "+logEvent(e), exception);
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

    /**
     * Overrides MongoDB local/remote IP 127.0.0.1, if Logstash Event contains "server_ip".
     * 
     * @param e - Logstash Event
     * @param record - Record after parsing.
     */
    private void correctIPs(Event e, Record record) {
        // Override "(NONE)" IP, if not filterd, as it's internal command by MongoDB.
        // Note: IP needs to be in ipv4/ipv6 format
        SessionLocator sessionLocator = record.getSessionLocator();
        String sessionServerIp = sessionLocator.getServerIp();
        if (LOCAL_IP_LIST.contains(sessionServerIp)
                || sessionServerIp.equalsIgnoreCase("(NONE)")) {
            if (e.getField("server_ip") instanceof String) {
                String ip = e.getField("server_ip").toString();
                if (ip != null) {
                    if (Util.isIPv6(ip)){
                        sessionLocator.setServerIpv6(ip);
                        sessionLocator.setIpv6(true);
                    } else {
                        sessionLocator.setServerIp(ip);
                        sessionLocator.setIpv6(false);
                    }
                } else if (sessionServerIp.equalsIgnoreCase("(NONE)")) {
                    sessionLocator.setServerIp("0.0.0.0");
                }
            }
        }
        
        String sessionClientIp = sessionLocator.getClientIp();
        if (LOCAL_IP_LIST.contains(sessionClientIp)
                || sessionLocator.getClientIp().equalsIgnoreCase("(NONE)")) { 
            sessionLocator.setClientIp(sessionLocator.getServerIp()); // as clientIP & serverIP were equal
        }
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
