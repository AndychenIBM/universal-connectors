package com.ibm.guardium.universalconnector.transformer;

import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.ibm.guardium.proto.datasource.Datasource;
import com.ibm.guardium.universalconnector.common.Utilities;
import com.ibm.guardium.universalconnector.common.structures.*;
import com.ibm.guardium.universalconnector.exceptions.GuardUCInvalidRecordException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

public class JsonRecordTransformer implements RecordTransformer {

    private static Log log = LogFactory.getLog(JsonRecordTransformer.class);
    private static final String LANG_TYPE_FREE_TEXT = "FREE_TEXT"; // for parser "FREE_TEXT"

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
                    .setSessionId(record.getSessionId().hashCode())//todo: check with Tim Session id issue
                    .setRequestId(record.getSessionId().hashCode())//todo: check with Tim Request id issue
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

        // mandatory field - no need to build without it
        if (isEmpty(record.getSessionId())){
            throw new GuardUCInvalidRecordException("Invalid sessionId value "+record.getSessionId());
        }

        Datasource.Session_start.Builder builder = Datasource.Session_start.newBuilder()
                .setSessionLocator(sessionLocator)
                .setTimestamp(Utilities.getTimestamp(record.getTime()))
                .setAccessor(accessor)
                .setProcessId(record.getSessionId())
                .setSessionId(record.getSessionId().hashCode());

        // optional fields - only set them if they have some value
        if (!isEmpty(record.getAppUserName())){
            builder.setAppUserName(record.getAppUserName());
        }

        if (isEmpty(record.getDbName())){
            builder.setDbName(record.getDbName());
        }

        return builder.build();
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
//        exceptionMsg.setAPPUSERNAME("AppUserNameFromException");
//        exceptionMsg.setCount(25);
//        exceptionMsg.setDBPASSWORDHASH("PasswordHashString");
        exceptionMsg.setDBPROTOCOL((accessor != null)?accessor.getServerType():null);

        exceptionMsg.setDBUSER((accessor != null)?accessor.getDbUser():null);
        exceptionMsg.setDESCRIPTION(recordException.getDescription());
//        exceptionMsg.setERRORCAUSE("ErrorCauseString");
        exceptionMsg.setEXCEPTIONTYPEID(recordException.getExceptionTypeId());

        exceptionMsg.setSQLSTRING(recordException.getSqlString());
        exceptionMsg.setSESSIONID(record.getSessionId());

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
            Datasource.GDM_field gdmField = Datasource.GDM_field.newBuilder().setName(field).build();
            gdmSentenceBuilder.addFields(gdmField);
        }

        // handle objects
        List<SentenceObject> objects = sentence.getObjects()!=null ? sentence.getObjects() : Collections.EMPTY_LIST;
        for (SentenceObject object : objects) {
            Datasource.GDM_object gdmObject = Datasource.GDM_object.newBuilder().setName(object.getName()).setSchema(object.getSchema()).build();
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

    public Datasource.Accessor buildAccessor(Record record) {
        // dataType specifies if sniffer should parse sql (type=TEXT)
        // or just enter to guardium the fields (sentences/object/etc) as is (type=CONSTRUCT)
        Datasource.Application_data.Data_type dataType = getDataType(record);

        // mandatory fields - no need to build without it
        Accessor ra = record.getAccessor();
        if (isEmpty(ra.getDbUser())){
            throw new GuardUCInvalidRecordException("Invalid getDbUser value "+ra.getDbUser());
        }
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
                .setDbUser(ra.getDbUser())
                .setServerType(ra.getServerType())
                .setDbProtocol(ra.getDbProtocol())
                .setLanguage(getLanguageType(record))
                .setType(dataType)
                .setDatasourceType(Datasource.Application_data.Datasource_type.UNI_CON);

        // optional fields - no need to set if they are empty;
        if (!isEmpty(ra.getServerOs())) {
            builder.setServerOs(ra.getServerOs());
        }
        if (!isEmpty(ra.getClientHostName())) {
            builder.setClientHostname(ra.getClientHostName());
        }
        if (!isEmpty(ra.getClientHostName())) {
            builder.setServerHostname(ra.getServerHostName());
        }
        if (!isEmpty(ra.getClientHostName())) {
            builder.setCommProtocol(ra.getCommProtocol());
        }
        if (!isEmpty(ra.getClientHostName())) {
            builder.setDbProtocolVersion(ra.getDbProtocolVersion());
        }
        if (!isEmpty(ra.getClientHostName())) {
            builder.setSourceProgram(ra.getSourceProgram());
        }
        if (!isEmpty(ra.getClientHostName())) {
            builder.setServerDescription(ra.getServerDescription());
        }
        if (!isEmpty(ra.getClientHostName())) {
            builder.setServiceName(ra.getServiceName());
        }

        return builder.build();
    }


    public Datasource.Session_locator buildSessionLocator(Record record){

        SessionLocator rsl = record.getSessionLocator();
        String clientIp = rsl.isIpv6() ? rsl.getClientIpv6() : rsl.getClientIp();
        String serverIp = rsl.isIpv6() ? rsl.getServerIpv6() : rsl.getServerIp();
        Datasource.Session_locator sessionLocator = Datasource.Session_locator.newBuilder()
                .setClientIp(convert_ipstr_to_int(clientIp))
                .setClientPort(rsl.getClientPort())
                .setServerIp(convert_ipstr_to_int(serverIp))
                .setServerPort(rsl.getServerPort())
                .build();
        return sessionLocator;
    }

    public static int convert_ipstr_to_int(String ip){
        int ret = 0;
        String[] segs = ip.split("\\.");

        if (segs!=null && segs.length>0){

            for (int i = segs.length-1; i >= 0; i--){
                int value = 0;
                try{
                    value = Integer.valueOf(segs[i]);
                    value = value << 8*i;
                }catch (java.lang.Exception e){
                    log.error("Failed to translate string IP to number, IP is "+ip+" section is "+segs[i], e);
                }
                ret += value ;
            }
        }

        return ret;
    }

    public Datasource.Application_data buildAppplicationData(Record record, Datasource.Session_locator sessionLocator) {

        Datasource.Application_data.Data_type dataType = getDataType(record);
        if (Datasource.Application_data.Data_type.TEXT.equals(dataType) && isEmpty(record.getAccessor().getLanguage())){
            throw new GuardUCInvalidRecordException("Invalid getLanguage() value "+record.getAccessor().getLanguage());
        }

        Datasource.Application_data.Builder builder = Datasource.Application_data.newBuilder()
                .setType(dataType)
                .setLanguage(getLanguageType(record))
                .setSessionLocator(sessionLocator)
                .setDatasourceType(Datasource.Application_data.Datasource_type.UNI_CON)
                .setTimestamp(Utilities.getTimestamp(record.getTime()));

        if (!isEmpty(record.getAppUserName())){
            builder.setApplicationUser(record.getAppUserName());
        }

        Data rd = record.getData();
        if(rd != null) {
            if (rd.isUseConstruct()) {
                Datasource.GDM_construct gdmConstruct = buildConstruct(rd);
                builder.setConstruct(gdmConstruct);
            } else {
                builder.setText(rd.getOriginalSqlCommand());
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

    public static Datasource.Application_data.Language_type getLanguageType(Record record){
        String langType = (record.getData()!=null && record.getData().isUseConstruct()) ? LANG_TYPE_FREE_TEXT : record.getAccessor().getLanguage();
        try {
            return Datasource.Application_data.Language_type.valueOf(langType.toUpperCase());
        } catch (Exception e){
            log.error("Failed to parse languageType "+langType+", return default FREE_TEXT");
            return Datasource.Application_data.Language_type.FREE_TEXT;
        }
    }

    public static Datasource.Application_data.Data_type getDataType(Record record){
        // only if "do not use construct" was set explicitly ( meaning "parse sql instead of passed objects" )
        if (record.getData()!=null && !record.getData().isUseConstruct()){
            return Datasource.Application_data.Data_type.TEXT;
        } else {
            return Datasource.Application_data.Data_type.CONSTRUCT;
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
