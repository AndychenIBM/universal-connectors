package com.ibm.guardium.universalconnector.transformer;

import com.google.gson.Gson;
import com.ibm.guardium.proto.datasource.Datasource;
import com.ibm.guardium.universalconnector.common.Utilities;
import com.ibm.guardium.universalconnector.commons.structures.*;
import com.ibm.guardium.universalconnector.exceptions.GuardUCInvalidRecordException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sun.net.util.IPAddressUtil;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class JsonRecordTransformer implements RecordTransformer {

    public static final String UC_PROTOCOL_PREFIX = "UC: ";
    private static Log log = LogFactory.getLog(JsonRecordTransformer.class);
    private static final String LANG_TYPE_FREE_TEXT = "FREE_TEXT"; // for parser "FREE_TEXT"
    public static String get(String envName, String defaultVal) {
        String gEnv = System.getenv(envName);
        if (null == gEnv || gEnv.isEmpty()) {
            gEnv = System.getProperty(envName);
        }
        if (null == gEnv || gEnv.isEmpty()) {
            gEnv = defaultVal;
        }
        return gEnv;
    }
    @Override
    public List<Datasource.Guard_ds_message> transform(String recordStr) {

        List<Datasource.Guard_ds_message> messages = new LinkedList<>();

        Record record = new Gson().fromJson(recordStr, Record.class);

        // from each audit log record we need to build 2 proto messages -
        // session_start and one of client_request or exception

        // 1. Session_Start message
        Datasource.Session_locator sessionLocator = buildSessionLocator(record);
        Datasource.Accessor accessor = buildAccessor(record);
        Datasource.Session_start sessionStart = buildSessionStart(record, sessionLocator, accessor);

        Datasource.Guard_ds_message startMsg = Datasource.Guard_ds_message.newBuilder()
                .setType(Datasource.Guard_ds_message.Type.SESSION_START)
                .setSessionStart(sessionStart)
                .build();

        messages.add(startMsg);


        // 2. Client_request or exception
        if(record.isException()){
            // build exception - the one that has data on errors
            Datasource.Exception exception = buildExceptionData(record, sessionLocator);
            Datasource.Guard_ds_message exceptionMsg = Datasource.Guard_ds_message.newBuilder()
                    .setType(Datasource.Guard_ds_message.Type.EXCEPTION)
                    .setException(exception)
                    .build();

            messages.add(exceptionMsg);
        }else {
            // build client request - the one that contains actual sql data
            Datasource.Application_data appData = buildAppplicationData(record, sessionLocator);

            Datasource.Client_request clientRequest = Datasource.Client_request.newBuilder()
                    .setSessionId(getSessionIdForSniffer(record.getSessionId()))
                    .setData(appData)
                    .build();

            Datasource.Guard_ds_message sqlMsg = Datasource.Guard_ds_message.newBuilder()
                    .setType(Datasource.Guard_ds_message.Type.CLIENT_REQUEST)
                    .setClientRequest(clientRequest)
                    .build();

            messages.add(sqlMsg);
        }
        return messages;
    }

    private boolean isEmpty(String value){
        return value==null || value.trim().length()==0;
    }

    public Datasource.Session_start buildSessionStart(Record record, Datasource.Session_locator sessionLocator, Datasource.Accessor accessor){

        Datasource.Session_start.Builder builder = Datasource.Session_start.newBuilder()
                .setSessionLocator(sessionLocator)
                .setTimestamp(Utilities.getTimestamp(record.getTime()))
                .setAccessor(accessor)
                .setSessionId(getSessionIdForSniffer(record.getSessionId()))
                .setTerminalId(get("TENANT_ID","TNT_ATGPHITOV3JEIXUXK8LTGR"))
                .setConfigId(get("CONFIG_ID","5d9f48d097ea6054a51f6b98"));
//              .setProcessId(record.getSessionId())

        // optional fields - only set them if they have some value
        trimAndSetIfNotEmpty(builder::setAppUserName, record::getAppUserName);
        trimAndSetIfNotEmpty(builder::setDbName, record::getDbName);

        return builder.build();
    }

    private static int getSessionIdForSniffer(String sessionId){
        if (sessionId==null){
            return 0;
        }
        return sessionId.hashCode();
    }

    /**
     *
     * This method will send an exception message based on the
     * error code which is returned from the database server
     * An example would be "table not found" exception.
     *
     */
    public static Datasource.Exception buildExceptionData(Record record,Datasource.Session_locator sessionLocator)  {
        // Build the session locator information to be put into the exception message
        // message. This must match the open session message

        //Use the builder to create the structure
        Datasource.Exception.Builder exceptionMsg = Datasource.Exception.newBuilder();

        ExceptionRecord recordException = record.getException();
        //Add Required fields
        exceptionMsg.setSession(sessionLocator);
        exceptionMsg.setTimestamp(Utilities.getTimestamp(record.getTime()));
        Accessor accessor = record.getAccessor();

        //Add Optional fields
        if (accessor!=null) {
            trimAndSet(exceptionMsg::setDBPROTOCOL, accessor::getServerType);
            trimAndSet(exceptionMsg::setDBUSER, accessor::getDbUser);
        }
        trimAndSetIfNotEmpty(exceptionMsg::setDESCRIPTION, recordException::getDescription); // description is optionals, other fields are not
        trimAndSet(exceptionMsg::setEXCEPTIONTYPEID, recordException::getExceptionTypeId);

        trimAndSet(exceptionMsg::setSQLSTRING, recordException::getSqlString);

        // must make sure sessionId in session start is aligned with sessionId in exception
        // per Tim's suggestion put here same hash that we set in session start
        // otherwise it causes problems in sniffer and exception is not stored in guardium
        exceptionMsg.setSESSIONID(""+getSessionIdForSniffer(record.getSessionId()));

        return exceptionMsg.build();
    }

    public Datasource.GDM_construct buildConstruct(Data recordData) {
        Datasource.GDM_construct.Builder gdmConstructBuilder = Datasource.GDM_construct.newBuilder();

        Construct construct = recordData.getConstruct();
        List<Sentence> sentences = recordData.getConstruct()!=null ? recordData.getConstruct().getSentences() : Collections.EMPTY_LIST;
        for (Sentence sentence : sentences) {
            Datasource.GDM_sentence gdmSentence = buildSentence(sentence);
            gdmConstructBuilder.addSentences(gdmSentence);
        }

        int sentenceType = 0;// todo: check with Tim what are the values
        gdmConstructBuilder.setFullSql(construct.getFullSql()).setStatementType(sentenceType);
        if (!isEmpty(construct.getRedactedSensitiveDataSql())){
            gdmConstructBuilder.setOriginalSql(construct.getRedactedSensitiveDataSql());
        }

        return gdmConstructBuilder.build();
    }

    public Datasource.GDM_sentence buildSentence(Sentence sentence) {

        Datasource.GDM_sentence.Builder  gdmSentenceBuilder = Datasource.GDM_sentence.newBuilder()
                .setVerb(sentence.getVerb());

        // handle fields
        List<String> fields = sentence.getFields()!=null ? sentence.getFields(): Collections.EMPTY_LIST;
        for (String field : fields) {
            if (field!=null){
                field = field.trim();
            }
            Datasource.GDM_field gdmField = Datasource.GDM_field.newBuilder().setName(field).build();
            gdmSentenceBuilder.addFields(gdmField);
        }

        // handle objects
        List<SentenceObject> objects = sentence.getObjects()!=null ? sentence.getObjects() : Collections.EMPTY_LIST;
        for (SentenceObject object : objects) {
            Datasource.GDM_object.Builder builder = Datasource.GDM_object.newBuilder();
            trimAndSet(builder::setName, object::getName);
            trimAndSetIfNotEmpty(builder::setSchema, object::getSchema);
            Datasource.GDM_object gdmObject = builder.build();
            gdmSentenceBuilder.addObjects(gdmObject);
        }

        // handle descendants
        List<Sentence> descendants = sentence.getDescendants()!=null ? sentence.getDescendants() : Collections.EMPTY_LIST;
        for (Sentence descendant : descendants) {
            Datasource.GDM_sentence gdmDescendant = buildSentence(descendant);
            gdmSentenceBuilder.addDescendants(gdmDescendant);
        }

        Datasource.GDM_sentence gdmSentence = gdmSentenceBuilder.build();

        return gdmSentence;
    }

    /**
     * Set "must have/required" property value after trimming it, if null - exception will be throws as DS does not except nulls
     * we prefer to get this exception, cause in those cases - data should be sent to sniffer and error will be written to our logs.
     * This function should be used on required properties as they should not be null.
     * @param apiToCall
     * @param stringInputApi
     */
    public static void trimAndSet(Consumer<String> apiToCall, Supplier<String> stringInputApi){
            //Function<String, com.google.protobuf.GeneratedMessage.Builder<Datasource.Accessor.Builder>> apiToCall, Supplier<String> stringInputApi){
        String value = stringInputApi.get();
        if (value!=null) {
            value = value.trim();
        }
        apiToCall.accept(value);
    }

    /**
     * Trim and set only of property value is not empty, should be used for optional property value
     * @param apiToCall
     * @param stringInputApi
     */
    public static void trimAndSetIfNotEmpty(Consumer<String> apiToCall, Supplier<String> stringInputApi){
        String value = stringInputApi.get();
        if (value!=null && value.trim().length()>0){
            apiToCall.accept(value.trim());
        }
    }

    public Datasource.Accessor buildAccessor(Record record) {
        // dataType specifies if sniffer should parse sql (type=TEXT)
        // or just enter to guardium the fields (sentences/object/etc) as is (type=CONSTRUCT)
        Datasource.Application_data.Data_type dataType = getAccessorDataType(record.getAccessor());

        // mandatory fields - no need to build without it
        Accessor ra = record.getAccessor();
//        if (isEmpty(ra.getDbUser())){
//            throw new GuardUCInvalidRecordException("Invalid getDbUser value "+ra.getDbUser());
//        }
        if (Datasource.Application_data.Data_type.CONSTRUCT.equals(dataType) && isEmpty(ra.getServerType())){
            throw new GuardUCInvalidRecordException("Invalid getServerType() value "+ra.getServerType());
        }
        if (isEmpty(ra.getDbProtocol())){ // method used to get audits
            throw new GuardUCInvalidRecordException("Invalid getDbProtocol() value "+ra.getDbProtocol());
        }
        if (Datasource.Application_data.Data_type.TEXT.equals(dataType) && isEmpty(ra.getLanguage())){
            throw new GuardUCInvalidRecordException("Invalid getLanguage() value "+ra.getLanguage());
        }

        Datasource.Accessor.Builder builder = Datasource.Accessor.newBuilder()
                .setDbProtocol(UC_PROTOCOL_PREFIX+ra.getDbProtocol())
                .setLanguage(getLanguageType(record.getAccessor()))
                .setType(dataType)
                .setDatasourceType(Datasource.Application_data.Datasource_type.UNI_CON);

        trimAndSetIfNotEmpty(builder::setDbUser, ra::getDbUser); // we saw that for mysql plugin that uses sniffer parsing this value can be null and it is OK
        trimAndSetIfNotEmpty(builder::setServerType, ra::getServerType);

        // optional fields - no need to set if they are empty;
        trimAndSetIfNotEmpty(builder::setServerOs, ra::getServerOs);
        trimAndSetIfNotEmpty(builder::setClientHostname, ra::getClientHostName);
        trimAndSetIfNotEmpty(builder::setCommProtocol, ra::getCommProtocol);
        trimAndSetIfNotEmpty(builder::setDbProtocolVersion, ra::getDbProtocolVersion);
        trimAndSetIfNotEmpty(builder::setSourceProgram, ra::getSourceProgram);
        trimAndSetIfNotEmpty(builder::setServerDescription, ra::getServerDescription);
        trimAndSetIfNotEmpty(builder::setServiceName, ra::getServiceName);
        trimAndSetIfNotEmpty(builder::setOsUser, ra::getOsUser);

        if (!isEmpty(ra.getServerHostName())) {
            trimAndSetIfNotEmpty(builder::setServerHostname, ra::getServerHostName);
        } else {
            // try to put server ip if host is empty, server ip may be ipv6 or ipv4, ipv6 flag means that at least one of ips is ipv6, no necessarily both
            String serverIp = record.getSessionLocator().getServerIp();
            if (record.getSessionLocator().isIpv6() && record.getSessionLocator().getServerIpv6()!=null && record.getSessionLocator().getServerIpv6().length()>0){
                serverIp = record.getSessionLocator().getServerIpv6();
            }
            builder.setServerHostname(serverIp);
        }

        return builder.build();
    }


    public Datasource.Session_locator buildSessionLocator(Record record){

        SessionLocator rsl = record.getSessionLocator();
        Datasource.Session_locator.Builder sessionBuilder =
                Datasource.Session_locator.newBuilder().setIsIpv6(rsl.isIpv6());

        // GRD-44925 - when both ports are not sent to sniffer - it will use sessionId to decide whether to create new session or not
        // in all other cases session id value is ignored by sniffer and only combination of ips and ports is used for session creation
        if (rsl.getClientPort()!=SessionLocator.PORT_DEFAULT){
            sessionBuilder.setClientPort(rsl.getClientPort());
        }

        if (rsl.getServerPort()!=SessionLocator.PORT_DEFAULT){
            sessionBuilder.setServerPort(rsl.getServerPort());
        }

        if (!rsl.isIpv6()){
            sessionBuilder.setServerIp(convert_ipstr_to_int(rsl.getServerIp())).setClientIp(convert_ipstr_to_int(rsl.getClientIp()));
        } else {
            // having ipv6 flag may mean that only one of ips is ipv6
            if (rsl.getServerIpv6()!=null && rsl.getServerIpv6().length()>0){
                sessionBuilder.setServerIpv6(rsl.getServerIpv6());
            } else {
                sessionBuilder.setServerIp(convert_ipstr_to_int(rsl.getServerIp()));
            }
            if (rsl.getClientIpv6()!=null && rsl.getServerIpv6().length()>0){
                sessionBuilder.setClientIpv6(rsl.getClientIpv6());
            } else {
                sessionBuilder.setClientIp(convert_ipstr_to_int(rsl.getClientIp()));
            }
        }
        return sessionBuilder.build();
    }

//    public static int convert_ipstr_to_int(String ip){
//        int ret = 0;
//        String[] segs = ip.split("\\.");
//
//        if (segs!=null && segs.length>0){
//
//            for (int i = segs.length-1; i >= 0; i--){
//                int value = 0;
//                try{
//                    value = Integer.valueOf(segs[i]);
//                    value = value << 8*i;
//                }catch (java.lang.Exception e){
//                    log.error("Failed to translate string IP to number, IP is "+ip+" section is "+segs[i], e);
//                }
//                ret += value ;
//            }
//        }
//
//        return ret;
//    }
    public static int convert_ipstr_to_int(String ip) {
        if (ip==null){
            System.out.println("Failed to translate ip to a number, ip string is "+ip);
            return 0;
        }
        byte[] arr = IPAddressUtil.textToNumericFormatV4(ip);
        if (arr==null){
            System.out.println("Failed to translate ip to a number, ip string is "+ip);
            return 0;
        }
        int num = convert_ip_bytes_to_int(arr);
        return num;
    }

    public static int convert_ip_bytes_to_int(byte[] bytes) {
        int ret = 0;
        if (bytes != null && bytes.length > 0) {
            for (int i = bytes.length - 1; i >= 0; i--) {
                int value = 0;
                try {
                    value = Byte.toUnsignedInt(bytes[i]);
                    value = value << 8 * i;
                } catch (java.lang.Exception e) {
                    System.out.println("Failed to translate string IP to number ip as string "+new String(bytes)+", section is "+value);
                    System.err.println(e);
                }
                ret += value;
            }
        }
        return ret;
    }

    public static boolean shouldGuardiumParseSql(Datasource.Application_data.Data_type dataType){
        return Datasource.Application_data.Data_type.TEXT.equals(dataType);
    }

    public Datasource.Application_data buildAppplicationData(Record record, Datasource.Session_locator sessionLocator){

        // can not set data type to TEXT ( meaning that guardium should parse data)
        // without specifying what is the language that should be parsed
        Datasource.Application_data.Data_type dataType = getAccessorDataType(record.getAccessor());
        if (shouldGuardiumParseSql(dataType) && isEmpty(record.getAccessor().getLanguage())){
            throw new GuardUCInvalidRecordException("Invalid getLanguage() value "+record.getAccessor().getLanguage());
        }

        Datasource.Application_data.Builder builder = Datasource.Application_data.newBuilder()
                .setType(dataType)
                .setLanguage(getLanguageType(record.getAccessor()))
                .setSessionLocator(sessionLocator)
                .setDatasourceType(Datasource.Application_data.Datasource_type.UNI_CON)
                .setTimestamp(Utilities.getTimestamp(record.getTime()));

        if (!isEmpty(record.getAppUserName())){
            builder.setApplicationUser(record.getAppUserName());
        }

        Data rd = record.getData();
        if(rd != null) {
            if (shouldGuardiumParseSql(dataType)) {
                trimAndSet(builder::setText, rd::getOriginalSqlCommand);
            } else {
                Datasource.GDM_construct gdmConstruct = buildConstruct(rd);
                builder.setConstruct(gdmConstruct);
            }
        }
        return builder.build();
    }

    /*
    * https://code.woboq.org/userspace/glibc/resolv/inet_pton.c.html#hex_digit_value
    * static int hex_digit_value (char ch)
        {
          if ('0' <= ch && ch <= '9')
            return ch - '0';
          if ('a' <= ch && ch <= 'f')
            return ch - 'a' + 10;
          if ('A' <= ch && ch <= 'F')
            return ch - 'A' + 10;
          return -1;
        }
    * */

    public static Datasource.Application_data.Language_type getLanguageType(Accessor recordAccessor){
        String langType = !shouldGuardiumParseSql(getAccessorDataType(recordAccessor)) ? LANG_TYPE_FREE_TEXT : recordAccessor.getLanguage();
        try {
            return Datasource.Application_data.Language_type.valueOf(langType.toUpperCase());
        } catch (Exception e){
            log.error("Failed to parse languageType "+langType+", return default FREE_TEXT");
            return Datasource.Application_data.Language_type.FREE_TEXT;
        }
    }

    public static Datasource.Application_data.Data_type getAccessorDataType(Accessor recordAccessor) throws GuardUCInvalidRecordException {
        if (Accessor.DATA_TYPE_GUARDIUM_SHOULD_PARSE_SQL.equalsIgnoreCase(recordAccessor.getDataType())){
            return Datasource.Application_data.Data_type.TEXT;
        } else if (Accessor.DATA_TYPE_GUARDIUM_SHOULD_NOT_PARSE_SQL.equalsIgnoreCase(recordAccessor.getDataType())){
            return Datasource.Application_data.Data_type.CONSTRUCT;
        } else {
            throw new GuardUCInvalidRecordException("Invalid Accessor data type: "+recordAccessor.getDataType());
        }
    }

    public static Datasource.Timestamp getTimestamp(Long time) {
        return Datasource.Timestamp.newBuilder()
                .setUnixTime(Utilities.getTimeUnixTime(time))
                .setUsec(Utilities.getTimeMicroseconds(time))
                .build();
    }

    public static void main(String[] args){
        System.out.println(1);
        JsonRecordTransformer transformer = new JsonRecordTransformer();
        System.out.println(2);
        String recordStr = "{\n" +
                "  \"sessionId\": \"{\\\"id\\\":{\\\"$type\\\":\\\"04\\\",\\\"$binary\\\":\\\"x9TSJiiQRvqVuupi3W/eCA==\\\"}}\",\n" +
                "  \"dbName\": \"test.inventory\",\n" +
                "  \"appUserName\": \"\",\n" +
                "  \"time\": 123456789,\n" +
                "\n" +
                "  \"sessionLocator\": {\n" +
                "    \"clientIp\": \"127.0.0.1\",\n" +
                "    \"clientPort\":61126,\n" +
                "    \"serverIp\": \"127.0.0.1\",\n" +
                "    \"serverPort\":27017,\n" +
                "    \"isIpv6\": false,\n" +
                "    \"clientIpv6\":\"\",\n" +
                "    \"serverIpv6\":\"\"\n" +
                "  },\n" +
                "\n" +
                "  \"accessor\": {\n" +
                "    \"dbUser\": \"BILL\",\n" +
                "    \"serverType\": \"MONGODB\",\n" +
                "    \"serverOs\": \"\",\n" +
                "    \"clientOs\": \"\",\n" +
                "    \"clientHostName\": \"\",\n" +
                "    \"serverHostName\": \"qa-db51\",\n" +
                "    \"commProtocol\": \"\" ,\n" +
                "    \"dbProtocol\": \"MONGODB WIRE PROTOCOL\",\n" +
                "    \"dbProtocolVersion\": \"\",\n" +
                "    \"osUser\": \"\",\n" +
                "    \"sourceProgram\": \"\",\n" +
                "    \"client_mac\": \"\",\n" +
                "    \"serverDescription\": \"\",\n" +
                "    \"serviceName\": \"\",\n" +
                "    \"language\": \"MONGO\"\n" +
                "  },\n" +
                "\n" +
                "  \"data\": {\n" +
                "    \"construct\": {\n" +
                "      \"sentences\": [\n" +
                "        {\n" +
                "          \"verb\": \"find\",\n" +
                "          \"objects\": [\n" +
                "            {\"name\": \"inventory\",\"type\": \"collection\",\"fields\": [],\"schema\": \"\"}\n" +
                "          ],\n" +
                "          \"descendants\": [],\n" +
                "          \"fields\": []\n" +
                "        }\n" +
                "      ],\n" +
                "      \"full_sql\": \"\",\n" +
                "      \"original_sql\": \"\"\n" +
                "    },\n" +
                "    \"timestamp\": 123456789,\n" +
                "    \"originalSqlCommand\": \"\",\n" +
                "    \"useConstruct\": \"true\"\n" +
                "  }\n" +
                "\n" +
                "}";
        System.out.println(3);
        Record record = new Gson().fromJson(recordStr, Record.class);

        System.out.println(4);
        // build session start msg
        Datasource.Accessor  accessor = transformer.buildAccessor(record); //todo: handle differently error/exception record - do not put data_type in accessor

        System.out.println(5);
        int X=0;
        List<Datasource.Guard_ds_message> messages = transformer.transform(recordStr);

        System.out.println("Result size"+messages.size());
    }

}
