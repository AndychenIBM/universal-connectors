package org.logstashplugins;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.Filter;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.LogstashPlugin;
import co.elastic.logstash.api.PluginConfigSpec;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

import com.google.gson.*;

import java.util.*;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.ibm.guardium.universalconnector.common.structures.*;
import com.ibm.guardium.universalconnector.common.Util;

// class name must match plugin name
@LogstashPlugin(name = "mysql_filter_guardium")
public class MySqlFilterGuardium implements Filter {

    public static final String LOG42_CONF="log4j2uc.properties";
    public static final String LOGSTASH_TAG_MYSQL_PARSE_ERROR = "_mysqlguardium_parse_error";
    public static final String LOGSTASH_TAG_MYSQL_IGNORE = "_mysqlguardium_ignore";
    
    public static final String EXCEPTION_TYPE_SQL_ERROR_STRING = "SQL_ERROR";
    public static final String EXCEPTION_TYPE_LOGIN_FAILED_STRING = "LOGIN_FAILED";
    
    private static final String QUERY_STRING = "Query";
    private static final String CONNECT_STRING = "Connect";
    
    private static final String CLASS_TYPE_GENERAL = "general";
    private static final String CLASS_TYPE_CONNECTION = "connection";
    private static final String CLASS_TYPE_AUDIT = "audit";
    private static final String CLASS_TYPE_TABLE_ACCESS = "table_access";
    private static final String DATA_TYPE_TABLE_ACCESS = "table_access_data";
    private static final String DATA_TYPE_GENERAL = "general_data";
    
    
    public static final String DATA_PROTOCOL_STRING = "MySQL native audit";
    public static final String UNKNOWN_STRING = "";
    public static final String SERVER_TYPE_STRING = "MySql";
    private static final String MASK_STRING = "?";

    private static final String DATE_FORMAT_ISO = "yyyy-MM-dd HH:mm:ss";
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(DATE_FORMAT_ISO);
    
    static {
        try {
            String uc_etc = System.getenv("UC_ETC");
            LoggerContext context = (LoggerContext) LogManager.getContext(false);
            File file = new File(uc_etc + File.separator + LOG42_CONF);
            context.setConfigLocation(file.toURI());
        } catch (Exception e){
            System.err.println("Failed to load log4j configuration "+e.getMessage());
            e.printStackTrace();
        }
    }
    private static Log log = LogFactory.getLog(MySqlFilterGuardium.class);
    
    public static final PluginConfigSpec<String> SOURCE_CONFIG =
            PluginConfigSpec.stringSetting("source", "message");

    private String id;
    private String sourceField;
    private final static Set<String> LOCAL_IP_LIST = new HashSet<>(
        Arrays.asList("127.0.0.1", "0:0:0:0:0:0:0:1"));

    public MySqlFilterGuardium(String id, Configuration config, Context context) {
        // constructors should validate configuration options
        this.id = id;
        this.sourceField = config.get(SOURCE_CONFIG);
    }

    @Override
    public Collection<Event> filter(Collection<Event> events, FilterMatchListener matchListener) {
        for (Event e : events) {
            if (e.getField("mysql_message") instanceof String) {
                String mysqlMsgString = e.getField("mysql_message").toString();
                int msgStrLen = mysqlMsgString.length();
                                   
                 // Remove last comma to get proper json string
                if (mysqlMsgString.charAt(msgStrLen-1) == ',')
                {
                    // remove last character (,)
                    mysqlMsgString = mysqlMsgString.substring(0, msgStrLen -1);
                }
                //log.warn(mysqlMsgString);
                try {
                    JsonObject inputJSON = (JsonObject) JsonParser.parseString(mysqlMsgString);
                    final String timestamp = inputJSON.get("timestamp").getAsString();
                    final int connection_id = inputJSON.get("connection_id").getAsInt();
                    final String class_type = inputJSON.get("class").getAsString();

                    if (class_type.equals(CLASS_TYPE_CONNECTION) && inputJSON.has("connection_data")) {
                        parseConnectionData(inputJSON);
                    }
                    else if (class_type.equals(CLASS_TYPE_GENERAL) || class_type.equals(CLASS_TYPE_TABLE_ACCESS)) {
                        Record record = new Record();
                        boolean validRecord = false;
                        
                        if (inputJSON.has(DATA_TYPE_TABLE_ACCESS)){
                            final JsonObject table_access_data = inputJSON.get(DATA_TYPE_TABLE_ACCESS).getAsJsonObject();
                            final String query = table_access_data.get("query").getAsString();
                            final String db_name = table_access_data.get("db").getAsString();
                            
                            Data data = new Data();
                            record.setData(data);
                            if (query != null)
                            {
                                data.setOriginalSqlCommand(query);
                                data.setUseConstruct(false);
                                validRecord = true;
                            }
                            record.setDbName(db_name);

                        } // end table_access_data
                        
                        else if (inputJSON.has(DATA_TYPE_GENERAL)) {
                            final JsonObject gen_data = inputJSON.get(DATA_TYPE_GENERAL).getAsJsonObject();
                            final String command = gen_data.get("command").getAsString(); 
                            final String query = gen_data.get("query").getAsString();
                            final int query_status = gen_data.get("status").getAsInt();
                        
                            if (command.equals(QUERY_STRING)) {
                            
                                if (query_status != 0) {
                                    // https://dev.mysql.com/doc/refman/8.0/en/error-message-components.html                                
                                    ExceptionRecord exceptionRecord = new ExceptionRecord();
                                    record.setException(exceptionRecord);

                                    exceptionRecord.setExceptionTypeId(EXCEPTION_TYPE_SQL_ERROR_STRING);
                                    exceptionRecord.setDescription("Error (" + query_status + ")"); 
                                    exceptionRecord.setSqlString(query);
                                    validRecord = true;
                                }
                            }
                            record.setDbName(UNKNOWN_STRING);

                        } // end general_data
                        if (validRecord) {
                            record.setSessionId(""+connection_id);
                            record.setAppUserName(UNKNOWN_STRING);
                                
                            long unixTime = getTimestamp(timestamp);
                            record.setTime(unixTime);
                                
                            record.setSessionLocator(parseSessionLocator(e, inputJSON));
                            record.setAccessor(parseAccessor(inputJSON));

                            this.correctIPs(e, record);
                                
                            final GsonBuilder builder = new GsonBuilder();
                            builder.serializeNulls();
                            final Gson gson = builder.create();
                            e.setField("Record", gson.toJson(record));
                        } // validRecord
                        else {
                            e.tag(LOGSTASH_TAG_MYSQL_IGNORE);
                        }
                        
                    } // end general or table_access class
                } catch (Exception exception) {
                    // TODO log event removed? 
                    //events.remove(e);
                    log.error("Error parsing mysql event " + logEvent(e), exception);
                    e.tag(LOGSTASH_TAG_MYSQL_PARSE_ERROR);
                }
                matchListener.filterMatched(e);
            }
        } // for events
        return events;
    }

    public static synchronized long getTimestamp(String dateString) throws ParseException {
        if (dateString == null){
            log.warn("DateString is null");
            return 0;
        }
        Date date = DATE_FORMATTER.parse(dateString);
        return date.getTime();
    }
        
    private static SessionLocator parseSessionLocator(Event e, JsonObject data) {
        SessionLocator sessionLocator = new SessionLocator();       
        String serverIp = "0.0.0.0";
        int serverPort = 0;

        if (e.getField("server_ip") instanceof String)
            serverIp = e.getField("server_ip").toString();
                               
        sessionLocator.setIpv6(false);
        sessionLocator.setClientIp(UNKNOWN_STRING);
        sessionLocator.setClientPort(0);
        sessionLocator.setClientIpv6(UNKNOWN_STRING);
        
        sessionLocator.setServerIp(serverIp); 
        sessionLocator.setServerPort(serverPort);
        sessionLocator.setServerIpv6(UNKNOWN_STRING);

        if (data.has("login")) {
            JsonObject login = data.getAsJsonObject("login");
            String address = login.get("ip").getAsString();
            int port = 0; // port not available, login.get("port").getAsInt();
            if (Util.isIPv6(address)) {
                sessionLocator.setIpv6(true);
                sessionLocator.setClientIpv6(address);
                sessionLocator.setClientPort(port);
                sessionLocator.setClientIp(UNKNOWN_STRING);
            } else { // ipv4 
                sessionLocator.setClientIp(address);
                sessionLocator.setClientPort(port);
                sessionLocator.setClientIpv6(UNKNOWN_STRING);
            }
        }
        return sessionLocator;
    }

    public static Accessor parseAccessor(JsonObject data) {
        Accessor accessor = new Accessor();

        accessor.setDbProtocol(DATA_PROTOCOL_STRING);
        accessor.setServerType(SERVER_TYPE_STRING);

        if (data.has("account")) {
            JsonObject login = data.getAsJsonObject("account");
            String user = login.get("user").getAsString();

            accessor.setDbUser(user);
        }
        accessor.setServerHostName(UNKNOWN_STRING); // populated from Event, later
        accessor.setSourceProgram(UNKNOWN_STRING); // populated from Event, later

        accessor.setLanguage("MYSQL");
        accessor.setType("TEXT");

        accessor.setClient_mac(UNKNOWN_STRING);
        accessor.setClientHostName(UNKNOWN_STRING);
        accessor.setClientOs(UNKNOWN_STRING);
        accessor.setCommProtocol(UNKNOWN_STRING);
        accessor.setDbProtocolVersion(UNKNOWN_STRING);
        accessor.setOsUser(UNKNOWN_STRING);
        accessor.setServerDescription(UNKNOWN_STRING);
        accessor.setServerOs(UNKNOWN_STRING);
        accessor.setServiceName(UNKNOWN_STRING);

        return accessor;
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
    
   private void correctIPs(Event e, Record record) {
        // Override "(NONE)" IP, if not filterd, as it's internal command by MongoDB.
        // Note: IP needs to be in ipv4/ipv6 format
        SessionLocator sessionLocator = record.getSessionLocator();
        String sessionServerIp = sessionLocator.getServerIp();
        if (LOCAL_IP_LIST.contains(sessionServerIp)
                || sessionServerIp.equals("")
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
                || sessionClientIp.equals("")
                || sessionClientIp.equalsIgnoreCase("(NONE)")) { 
            sessionLocator.setClientIp(sessionLocator.getServerIp()); // as clientIP & serverIP were equal
        }
    }
    
    public static void parseConnectionData(JsonObject data) {
        String event = data.get("event").getAsString();
        if (event.equals("connect")) {
        } // end event connect
        else if (event.equals("disconnect")) {
        } // end event disconnect       
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
