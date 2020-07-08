package com.ibm.guardium;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.ibm.guardium.connector.Util;
import com.ibm.guardium.connector.structures.Accessor;
import com.ibm.guardium.connector.structures.Construct;
import com.ibm.guardium.connector.structures.Data;
import com.ibm.guardium.connector.structures.ExceptionRecord;
import com.ibm.guardium.connector.structures.Record;
import com.ibm.guardium.connector.structures.Sentence;
import com.ibm.guardium.connector.structures.SentenceObject;
import com.ibm.guardium.connector.structures.SessionLocator;

public class Parser {
    public static final String DATA_PROTOCOL_STRING = "Logstash";
    public static final String UNKOWN_STRING = "n/a";
    public static final String SERVER_TYPE_STRING = "MONGODB";
    private static final String MASK_STRING = "?";
    public static final String EXCEPTION_TYPE_AUTHORIZATION_STRING = "SQL_ERROR";
    public static final String EXCEPTION_TYPE_AUTHENTICATION_STRING = "LOGIN_FAILED";
    /**
     * These arguments will not be redacted, as they only contain 
     * collection/field names rather than sensitive values.
     */
    public static Set<String> REDACTION_IGNORE_STRINGS = new HashSet<>(
            Arrays.asList("from", "localField", "foreignField", "as", "connectFromField", "connectToField"));

    private static String DATE_FORMAT_ISO = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private static SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(DATE_FORMAT_ISO);

    /**
     * Parses a MongoDB native audit sent over syslog. Format looks as the
     * database.profiler.
     * 
     * @param data
     * @return
     */
    public static String Parse(final JsonObject data) {

        try {

            final Construct construct = Parser.ParseAsConstruct(data);

            final GsonBuilder builder = new GsonBuilder();
            builder.setPrettyPrinting().serializeNulls();
            final Gson gson = builder.create();
            return gson.toJson(construct);

        } catch (final Exception e) {
            throw e;
        }

    }

    /**
     * Parses a MongoDB native audit sent over syslog.
     * 
     * JSON format appreas after "mongod: " string and expected format is the same
     * as mongodb database.profiler. For example: { "atype": "authCheck", "ts": {
     * "$date": "2020-01-14T10:46:02.431-0500" }, "local": { "ip": "127.0.0.1",
     * "port": 27017 }, "remote": { "ip": "127.0.0.1", "port": 33708 }, "users": [],
     * "roles": [], "param": { "command": "find", "ns": "test.bios", "args": {
     * "find": "bios", "filter": {}, "lsid": { "id": { "$binary":
     * "hg6ugx4ASiGWKSPiDRlEFw==", "$type": "04" } }, "$db": "test" } }, "result": 0
     * },
     * 
     * @return Construct Used by Parse and for easier testing, as well
     * @author Tal Daniel
     */
    public static Construct ParseAsConstruct(final JsonObject data) {
        try {
            final Sentence sentence = Parser.parseSentence(data);
            
            final Construct construct = new Construct();
            construct.sentences.add(sentence);
            
            construct.setFull_sql(data.toString());
            
            if (data.get("atype").getAsString().equals("authCheck")) {
                // redact, though docs state args may be already redacted.
                Parser.RedactWithExceptions(data); // Warning: overwrites data.param.args
            }
            
            construct.setOriginal_sql(data.toString());
            return construct;
        } catch (final Exception e) {
            throw e;
        }
    }
    
    protected static Sentence parseSentence(final JsonObject data) {
        
        Sentence sentence = null;
        
        final String atype = data.get("atype").getAsString();
        final JsonObject param = data.get("param").getAsJsonObject();
        
        switch (atype) {
            case "authCheck":
                final String command = param.get("command").getAsString();
                final JsonObject args = param.getAsJsonObject("args");

                // + main object
                sentence = new Sentence(command);
                if (args.has(command)) {
                    final SentenceObject sentenceObject = new SentenceObject(args.get(command).getAsString());
                    sentence.objects.add(sentenceObject);
                }

                switch (command) {
                    case "aggregate":
                        /*
                         * Assumes no inner-lookups; only sequential stages in pipeline.
                         */
                        final JsonArray pipeline = args.getAsJsonArray("pipeline");
                        if (pipeline != null && pipeline.size() > 0) {
                            for (final JsonElement stage : pipeline) {
                                // handle * lookups
                                // + object if stage has $lookup or $graphLookup: { from: obj2 }
                                JsonObject lookupStage = null;

                                if (stage.getAsJsonObject().has("$lookup")) {
                                    lookupStage = stage.getAsJsonObject().getAsJsonObject("$lookup");
                                } else if (stage.getAsJsonObject().has("$graphLookup")) {
                                    lookupStage = stage.getAsJsonObject().getAsJsonObject("$graphLookup");
                                }

                                if (lookupStage != null && lookupStage.has("from")) {
                                    final SentenceObject lookupStageObject = new SentenceObject(
                                            lookupStage.get("from").getAsString());
                                    // + object
                                    sentence.objects.add(lookupStageObject);
                                }
                            }
                        }
                    default: // find, insert, delete, update, ...
                        break; // already done before switch
                }
                break;
            /* case "createCollection":
            case "dropCollection":
                final String ns = param.get("ns").getAsString();
                final String[] nsArray = ns.split("\\.");
                final String db = nsArray[0];
                final String collection = nsArray[1];
                sentence = new Sentence(atype); // atype is command
                final SentenceObject sentenceObject = new SentenceObject(collection, db);
                    sentence.objects.add(sentenceObject);
                break; */
            default:
                return null; // NOTE: not parsed
        }

        return sentence;
    }

    public static Record parseRecord(final JsonObject data) throws ParseException {
        // TODO get param.args.lsid, or fabricate
        Record record = new Record();

        final JsonObject param = data.get("param").getAsJsonObject();
        final JsonObject args = param.getAsJsonObject("args");
        final String result = data.get("result").getAsString(); // 0 success; 13/18 errors

        String sessionId = Parser.UNKOWN_STRING;
        if (args != null && args.has("lsid")) {
            final JsonObject lsid = args.getAsJsonObject("lsid");
            sessionId = lsid.getAsJsonObject("id").get("$binary").getAsString();
        }
        record.setSessionId(sessionId);

        String dbName = Parser.UNKOWN_STRING;
        if (args != null && args.has("$db")) {
            dbName = args.get("$db").getAsString();
        } else if (param != null && param.has("db")) { // in "authenticate" error message 
            dbName = param.get("db").getAsString();
        } else if (param != null && param.has("ns")) {
            final String ns = param.get("ns").getAsString(); 
            dbName = ns.split("\\.")[0]; // sometimes contains "."; fallback OK.
        }
        record.setDbName(dbName);
        record.setAppUserName(Parser.UNKOWN_STRING);

        record.setSessionLocator(Parser.parseSessionLocator(data));
        record.setAccessor(Parser.parseAccessor(data));

        if (result.equals("0")) {
            record.setData(Parser.parseData(data));
        } else { // 13, 18
            record.setException(Parser.ParseException(data, result));
        }

        // post populate fields:
        record.getAccessor().setServiceName(dbName); // FIXME/Notice: exists also in Record.dbName

        // set timestamp
        String dateString = Parser.parseTimestamp(data);
        long timestamp = Parser.getTime(dateString);
        record.setTime(timestamp);
        if (record.getData() != null) {
            record.getData().setTimestamp(timestamp);
        } // (else it's an exception)
        if (record.isException()) {
            record.getException().setTimestamp(String.valueOf(timestamp));
        }

        return record;
    }

    /**
     * Creates an ExceptionRecord to be used in Record, instead of Data.
     * @param data
     * @param resultCode
     * @return
     */
    private static ExceptionRecord ParseException(JsonObject data, String resultCode) {
        ExceptionRecord exceptionRecord = new ExceptionRecord();
        if (resultCode.equals("13")) {
            exceptionRecord.setExceptionTypeId(Parser.EXCEPTION_TYPE_AUTHORIZATION_STRING);
            exceptionRecord.setDescription("Unauthorized to perform the operation (13)");
            //  exceptionRecord.setSqlString(); DEFER

        } else if (resultCode.equals("18")) {
            exceptionRecord.setExceptionTypeId(Parser.EXCEPTION_TYPE_AUTHENTICATION_STRING);
            exceptionRecord.setDescription("Authentication Failed (18)");
        } else { // prep for unknown error code
            exceptionRecord.setExceptionTypeId(Parser.EXCEPTION_TYPE_AUTHORIZATION_STRING);
            exceptionRecord.setDescription("Error (" + resultCode + ")"); // let Guardium handle, if you'd like
        }

        exceptionRecord.setSqlString(data.toString()); // NOTE: no redaction
        // exceptionRecord.setTimestamp() is called later, as optimization
        return exceptionRecord;
    }

    public static Accessor parseAccessor(JsonObject data) {
        Accessor accessor = new Accessor();

        accessor.setDbProtocol(Parser.DATA_PROTOCOL_STRING);
        accessor.setServerType(Parser.SERVER_TYPE_STRING);

        String dbUsers = Parser.UNKOWN_STRING;
        if (data.has("users")) {
            JsonArray users = data.getAsJsonArray("users");
            dbUsers = "";
            if (users.size() > 0) {
                for (JsonElement user : users) {
                    dbUsers += user.getAsJsonObject().get("user").getAsString() + " ";
                }
            } else if (data.has("param")) { // users array is empty in "authenticate" exception; fetch from param.user:
                final JsonObject param = data.get("param").getAsJsonObject();
                if (param.has("user")) { // in authenticate event
                    dbUsers = param.get("user").getAsString();
                }
            }
        } 
        
        accessor.setDbUser(dbUsers);

        accessor.setServerHostName(Parser.UNKOWN_STRING); // populated from Event, later
        accessor.setSourceProgram(Parser.UNKOWN_STRING); // populated from Event, later

        accessor.setLanguage(Accessor.LANGUAGE_FREE_TEXT_STRING);
        accessor.setType(Accessor.TYPE_CONSTRUCT_STRING);

        accessor.setClient_mac(Parser.UNKOWN_STRING);
        accessor.setClientHostName(Parser.UNKOWN_STRING);
        accessor.setClientOs(Parser.UNKOWN_STRING);
        accessor.setCommProtocol(Parser.UNKOWN_STRING);
        accessor.setDbProtocolVersion(Parser.UNKOWN_STRING);
        accessor.setOsUser(Parser.UNKOWN_STRING);
        accessor.setServerDescription(Parser.UNKOWN_STRING);
        accessor.setServerOs(Parser.UNKOWN_STRING);
        accessor.setServiceName(Parser.UNKOWN_STRING);

        return accessor;
    }

    private static SessionLocator parseSessionLocator(JsonObject data) {
        SessionLocator sessionLocator = new SessionLocator();
        sessionLocator.setIpv6(false);

        sessionLocator.setClientIp(Parser.UNKOWN_STRING);
        sessionLocator.setClientPort(0);
        sessionLocator.setClientIpv6(Parser.UNKOWN_STRING);

        if (data.has("remote")) {
            JsonObject remote = data.getAsJsonObject("remote");
            String address = remote.get("ip").getAsString();
            int port = remote.get("port").getAsInt();
            if (Util.isIPv6(address)) {
                sessionLocator.setIpv6(true);
                sessionLocator.setClientIpv6(address);
                sessionLocator.setClientPort(port);
                sessionLocator.setClientIp(Parser.UNKOWN_STRING);
            } else { // ipv4 
                sessionLocator.setClientIp(address);
                sessionLocator.setClientPort(port);
                sessionLocator.setClientIpv6(Parser.UNKOWN_STRING);
            }
        }
        if (data.has("local")) {
            JsonObject local = data.getAsJsonObject("local");
            String address = local.get("ip").getAsString();
            int port = local.get("port").getAsInt();
            if (Util.isIPv6(address)) {
                sessionLocator.setServerIpv6(address);
                sessionLocator.setServerPort(port);
                sessionLocator.setServerIp(Parser.UNKOWN_STRING);
            } else { // IPv4
                sessionLocator.setServerIp(address);
                sessionLocator.setServerPort(port);
                sessionLocator.setServerIpv6(Parser.UNKOWN_STRING);
            }
        }
        return sessionLocator;
    }

    /**
     * Parses the query and returns a Data instance. Note: Setting timestamp
     * deferred, to be set by Parser.parseRecord().
     * 
     * @param inputJSON
     * @return
     * 
     * @see Data
     */
    public static Data parseData(JsonObject inputJSON) {
        Data data = new Data();
        data.setOriginalSqlCommand(Parser.UNKOWN_STRING); // TODO: remove if not used
        try {
            Construct construct = ParseAsConstruct(inputJSON);
            if (construct != null) {
                data.setConstruct(construct);
                data.setUseConstruct(true);

                if (construct.getFull_sql() == null) {
                    construct.setFull_sql(Parser.UNKOWN_STRING);
                }
                if (construct.getOriginal_sql() == null) {
                    construct.setOriginal_sql(Parser.UNKOWN_STRING);
                }
            }
        } catch (Exception e) {
            throw e;
        }
        return data;
    }

    public static String parseTimestamp(final JsonObject data) {
        String dateString = null;
        if (data.has("ts")) {
            dateString = data.getAsJsonObject("ts").get("$date").getAsString();
        }
        return dateString;
    }

    public static long getTime(String dateString) throws ParseException {
        Date date = DATE_FORMATTER.parse(dateString);
        return date.getTime();
    }

    /**
     * Redact except values of objects and verbs
     */
    static JsonElement RedactWithExceptions(JsonObject data) {

        final JsonObject param = data.get("param").getAsJsonObject();
        final String command = param.get("command").getAsString();
        final JsonObject args = param.getAsJsonObject("args");
        
        final JsonElement originalCollection = args.get(command);
        final JsonElement originalDB = args.get("$db");
        
        final JsonElement redactedArgs = Parser.Redact(args);
        
        // restore common field values not to redact
        args.remove(command);
        args.add(command, originalCollection);
        args.remove("$db");
        args.add("$db", originalDB);

        return redactedArgs;
    }

    /**
     * Redact/Sanitize sensitive information. For example all field values.
     * The result can be seen in reports like "Hourly access details" or "Long running queries". 
     * Note: data is transformed/changed, so use only after you don't need the data anymore, 
     * for example, after populating as String in full_sql.
     * @param data
     * @return
     */
    static JsonElement Redact(JsonElement data) {
        // if final-leaf value (string, number) return "?"
        // else {
            // if reserved word: Redact valueRedact 
            // }
            if (data.isJsonPrimitive()) {
                return new JsonPrimitive(Parser.MASK_STRING);
            }

            else if (data.isJsonArray()) { 
                JsonArray array = data.getAsJsonArray(); 
                    for (int i=0; i<array.size(); i++) {
                        JsonElement redactedElement = Parser.Redact(array.get(i));
                        array.set(i, redactedElement);
                    }
            } 
            else if (data.isJsonObject()) {
                JsonObject object = data.getAsJsonObject();
                final Set<String> keys = object.keySet();
                final Set<String> keysCopy = new HashSet<>(); // make a copy, as keys changes on every remove/add, below  
                for (String key : keys) {
                    keysCopy.add(key);
                }
                for (String key : keysCopy) { 
                    if (!REDACTION_IGNORE_STRINGS.contains(key)) {
                        JsonElement redactedValue = Redact(object.get(key));
                        object.remove(key);
                        object.add(key, redactedValue); 
                    } 
                }
            }

            return /* changed */ data;
    }
}