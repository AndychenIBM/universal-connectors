package com.ibm.guardium.universalconnector.transformer;

import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.ibm.guardium.proto.datasource.Datasource;
import com.ibm.guardium.universalconnector.transformer.RecordTransformer;
import com.ibm.guardium.universalconnector.transformer.jsonrecord.*;
import com.ibm.guardium.universalconnector.transformer.jsonrecord.Record;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

public class JsonRecordTransformer implements RecordTransformer {

    private static Log log = LogFactory.getLog(JsonRecordTransformer.class);

    public static String recordSampleMongo = "{\n" +
            "  \"sessionId\": \"{\\\"id\\\":{\\\"$type\\\":\\\"04\\\",\\\"$binary\\\":\\\"x9TSJiiQRvqVuupi3W/eCA==\\\"}}\",\n" +
            "  \"dbName\": \"test.inventory\",\n" +
            "  \"appUserName\": \"\",\n" +
            "  \"time\": 123456789,\n" +
            "\n" +
            "  \"sessionLocator\": {\n" +
            "    \"clientIp\": \"127.0.0.11\",\n" +
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
            "    \"language\": \"DB2\"\n" +
            "  },\n" +
            "\n" +
            "  \"data\": {\n" +
            "    \"construct\": {\n" +
            "      \"sentences\": [\n" +
            "        {\n" +
            "          \"verb\": \"find\",\n" +
            "          \"objects\": [\n" +
            "            {\"name\": \"inventory\",\"type\": \"collection\",\"fields\": [\"mongo_field_1\"],\"schema\": \"blabla\"}\n" +
            "          ],\n" +
            "          \"descendants\": [],\n" +
            "          \"fields\": [\"mongo_field_211\"]\n" +
            "        }\n" +
            "      ],\n" +
            "      \"full_sql\": \"full_sqlfull_sql\",\n" +
            "      \"original_sql\": \"original_sqloriginal_sql\"\n" +
            "    },\n" +
            "    \"timestamp\": 123456789,\n" +
            "    \"originalSqlCommand\": \"originalSqlCommandoriginalSqlCommand\",\n" +
            "    \"useConstruct\": true\n" +
            "  }\n" +
            "\n" +
            "}";

    @Override
    public List<Datasource.Guard_ds_message> transform(String recordStr) {
        //recordStr = recordSample;
        //recordStr = recordSampleMongo;

        List<Datasource.Guard_ds_message> messages = new LinkedList<>();

        Record record = new Gson().fromJson(recordStr, Record.class);

        Datasource.Session_locator   sessionLocator = buildSessionLocator(record);

        // build session start msg
        Datasource.Accessor  accessor = buildAccessor(record); //todo: handle differently error/exception record - do not put data_type in accessor from "data" json

        Datasource.Session_start sessionStart = buildSessionStart(record, sessionLocator, accessor);

        Datasource.Guard_ds_message startMsg = Datasource.Guard_ds_message.newBuilder()
                .setType(Datasource.Guard_ds_message.Type.SESSION_START)
                .setSessionStart(sessionStart)
                .build();

        messages.add(startMsg);

        // build client request - the one that contains actual sql data
        Datasource.Application_data  appData = buildAppplicationData(record, sessionLocator);

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

        return messages;
    }

    public Datasource.Session_start buildSessionStart(Record record, Datasource.Session_locator sessionLocator, Datasource.Accessor accessor){

        Datasource.Timestamp sessionStartTimestamp = Datasource.Timestamp.newBuilder().setUnixTime(record.getTime()).build();

        Datasource.Session_start sessionStart = Datasource.Session_start.newBuilder()
                .setSessionLocator(sessionLocator)
                .setTimestamp(sessionStartTimestamp)
                .setAccessor(accessor)
                .setProcessId(record.getSessionId())
                .setSessionId(record.getSessionId().hashCode())//todo: check with Tim session id issue
                .setDbName(record.getDbName())
                .setAppUserName(record.getAppUserName())
                .build();

        return sessionStart;
    }

    public Datasource.Application_data buildAppplicationData(Record record, Datasource.Session_locator sessionLocator) {
        Data rd = record.getData();
        Datasource.Timestamp timestamp = getTimeStamp(record);
        Datasource.Application_data.Data_type dataType = getDataType(record);
        Datasource.Application_data.Language_type langType = getLanguageType(/*record.getAccessor().getLanguage()*/"FREE_TEXT");
        Datasource.Application_data.Builder builder = Datasource.Application_data.newBuilder()
                .setType(dataType)
                .setLanguage(langType)
                .setSessionLocator(sessionLocator)
                .setDatasourceType(Datasource.Application_data.Datasource_type.UNI_CON)
                .setApplicationUser(record.getAppUserName())
                .setTimestamp(timestamp);

        if (rd.isUseConstruct()){
            Datasource.GDM_construct gdmConstruct = buildConstruct(rd);
            builder.setConstruct(gdmConstruct);
            builder.setType(Datasource.Application_data.Data_type.CONSTRUCT);
        } else {
            builder.setText(rd.getOriginalSqlCommand());
        }
        return builder.build();
    }

    public Datasource.Timestamp getTimeStamp(Record record) {
        return Datasource.Timestamp.newBuilder().setUnixTime(record.getData().getTimestamp()).build();
    }

    public Datasource.GDM_construct buildConstruct(Data recordData) {
        Datasource.GDM_construct.Builder gdmConstructBuilder = Datasource.GDM_construct.newBuilder();

        Construct construct = recordData.getConstruct();
        List<Sentence> sentences = recordData.getConstruct()!=null ? recordData.getConstruct().getSentences() : Collections.EMPTY_LIST;
        for (Sentence sentence : sentences) {
            Datasource.GDM_sentence gdmSentence = buildSentence(sentence);
            gdmConstructBuilder.addSentences(gdmSentence);
        }

        int sentenceType = 0;// todo: check with Jim what are the values
        gdmConstructBuilder.setFullSql(construct.getFull_sql()).setOriginalSql(construct.getOriginal_sql()).setStatementType(sentenceType).build();

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
            Datasource.GDM_field gdmField2 = Datasource.GDM_field.newBuilder().setName("TEST_FIELD_VALUES2130").setValue(ByteString.copyFrom("TEST_FIELD_VALUES_VALUE2121".getBytes())).build();
            gdmSentenceBuilder.addFields(gdmField2);
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
        Accessor ra = record.getAccessor();
        Datasource.Application_data.Data_type dataType = getDataType(record);
        Datasource.Application_data.Language_type languageType = getLanguageType(ra.getLanguage());
        Datasource.Accessor accessor = Datasource.Accessor.newBuilder()
                .setDbUser(ra.getDbUser())
                .setServerType(ra.getServerType())
                .setServerOs(ra.getServerOs())
                .setClientHostname(ra.getClientHostName())
                .setServerHostname(ra.getServerHostName())
                .setCommProtocol(ra.getCommProtocol())
                .setDbProtocolVersion(ra.getDbProtocolVersion())
                .setSourceProgram(ra.getSourceProgram())
                .setServerDescription(ra.getServerDescription())
                .setServiceName(ra.getServiceName())
                .setLanguage(languageType)
                .setType(dataType)
                .build();
        return accessor;
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

        log.debug("Translated string IP "+ip+" to number "+ret);

        return ret;
    }

    public static Datasource.Application_data.Language_type getLanguageType(String strType){
        return Datasource.Application_data.Language_type.valueOf(strType.toUpperCase());
    }

    public static Datasource.Application_data.Data_type getDataType(Record record){
//        if (record.getData()!=null && record.getData().isUseConstruct()){
        return Datasource.Application_data.Data_type.CONSTRUCT;
//        } else {
//            return Datasource.Application_data.Data_type.TEXT;
//        }
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
