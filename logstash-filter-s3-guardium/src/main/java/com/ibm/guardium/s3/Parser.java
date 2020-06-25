package com.ibm.guardium.s3;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.google.gson.*;
import com.ibm.guardium.s3.connector.structures.Accessor;
import com.ibm.guardium.s3.connector.structures.Construct;
import com.ibm.guardium.s3.connector.structures.Data;
import com.ibm.guardium.s3.connector.structures.Record;
import com.ibm.guardium.s3.connector.structures.Sentence;
import com.ibm.guardium.s3.connector.structures.SentenceObject;
import com.ibm.guardium.s3.connector.structures.SessionLocator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Parser {

    private static Log log = LogFactory.getLog(Parser.class);

    private static final String DATE_FORMAT_ISO = "yyyy-MM-dd'T'HH:mm:ss";// "yyyy-MM-dd'T'HH:mm:ssZ";
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(DATE_FORMAT_ISO);
    private static final String UNKNOWN_STRING = "N/A";
    private static final String S3_TYPE = "S3";
    private static final Gson   gson = new Gson();


    public static String CREATE_BUCKET_SAMPLE =
                    "{\n" +
                    "    \"eventVersion\": \"1.05\",\n" +
                    "    \"userIdentity\": {\n" +
                    "        \"type\": \"IAMUser\",\n" +
                    "        \"principalId\": \"AIDAJWW2XAIOY2WN3KAAM\",\n" +
                    "        \"arn\": \"arn:aws:iam::987076625343:user/ProxyTest\",\n" +
                    "        \"accountId\": \"987076625343\",\n" +
                    "        \"accessKeyId\": \"ASIA6LUS2AO7VUBWUK3O\",\n" +
                    "        \"userName\": \"ProxyTest\",\n" +
                    "        \"sessionContext\": {\n" +
                    "            \"attributes\": {\n" +
                    "                \"mfaAuthenticated\": \"false\",\n" +
                    "                \"creationDate\": \"2020-06-11T05:27:07Z\"\n" +
                    "            }\n" +
                    "        }\n" +
                    "    },\n" +
                    "    \"eventTime\": \"2020-06-11T13:09:40Z\",\n" +
                    "    \"eventSource\": \"s3.amazonaws.com\",\n" +
                    "    \"eventName\": \"CreateBucket\",\n" +
                    "    \"awsRegion\": \"us-east-1\",\n" +
                    "    \"sourceIPAddress\": \"85.250.36.92\",\n" +
                    "    \"userAgent\": \"[S3Console/0.4, aws-internal/3 aws-sdk-java/1.11.783 Linux/4.9.217-0.1.ac.205.84.332.metal1.x86_64 OpenJDK_64-Bit_Server_VM/25.252-b09 java/1.8.0_252 vendor/Oracle_Corporation]\",\n" +
                    "    \"requestParameters\": {\n" +
                    "        \"host\": [\n" +
                    "            \"s3.amazonaws.com\"\n" +
                    "        ],\n" +
                    "        \"bucketName\": \"bucketnewbucketkkkkk\"\n" +
                    "    },\n" +
                    "    \"responseElements\": null,\n" +
                    "    \"additionalEventData\": {\n" +
                    "        \"SignatureVersion\": \"SigV4\",\n" +
                    "        \"CipherSuite\": \"ECDHE-RSA-AES128-SHA\",\n" +
                    "        \"AuthenticationMethod\": \"AuthHeader\",\n" +
                    "        \"vpcEndpointId\": \"vpce-f40dc59d\"\n" +
                    "    },\n" +
                    "    \"requestID\": \"6899CAF621927929\",\n" +
                    "    \"eventID\": \"0130637f-d325-4a9b-a6c8-3a8cd57ecdbf\",\n" +
                    "    \"eventType\": \"AwsApiCall\",\n" +
                    "    \"recipientAccountId\": \"987076625343\",\n" +
                    "    \"vpcEndpointId\": \"vpce-f40dc59d\"\n" +
                    "}";

    public static final String GETOBJECT_EVENT =
                    "{\n" +
                    "    \"eventVersion\": \"1.07\",\n" +
                    "    \"userIdentity\": {\n" +
                    "        \"type\": \"IAMUser\",\n" +
                    "        \"principalId\": \"AIDAJWW2XAIOY2WN3KAAM\",\n" +
                    "        \"arn\": \"arn:aws:iam::987076625343:user/ProxyTest\",\n" +
                    "        \"accountId\": \"987076625343\",\n" +
                    "        \"accessKeyId\": \"ASIA6LUS2AO7Z4EHS6VL\",\n" +
                    "        \"userName\": \"ProxyTest\",\n" +
                    "        \"sessionContext\": {\n" +
                    "            \"attributes\": {\n" +
                    "                \"creationDate\": \"2020-06-10T09:15:19Z\",\n" +
                    "                \"mfaAuthenticated\": \"false\"\n" +
                    "            }\n" +
                    "        }\n" +
                    "    },\n" +
                    "    \"eventTime\": \"2020-06-10T12:49:15Z\",\n" +
                    "    \"eventSource\": \"s3.amazonaws.com\",\n" +
                    "    \"eventName\": \"GetObject\",\n" +
                    "    \"awsRegion\": \"us-east-1\",\n" +
                    "    \"sourceIPAddress\": \"85.250.36.92\",\n" +
                    "    \"userAgent\": \"[Mozilla/5.0 (Macintosh; Intel Mac OS X 10.14; rv:68.0) Gecko/20100101 Firefox/68.0]\",\n" +
                    "    \"requestParameters\": {\n" +
                    "        \"X-Amz-Date\": \"20200610T124914Z\",\n" +
                    "        \"bucketName\": \"guardiumdatalogig\",\n" +
                    "        \"X-Amz-Algorithm\": \"AWS4-HMAC-SHA256\",\n" +
                    "        \"response-content-disposition\": \"inline\",\n" +
                    "        \"X-Amz-SignedHeaders\": \"host\",\n" +
                    "        \"Host\": \"guardiumdatalogig.s3.us-east-1.amazonaws.com\",\n" +
                    "        \"X-Amz-Expires\": \"300\",\n" +
                    "        \"key\": \"Screen Shot 2020-05-31 at 15.48.34.png\"\n" +
                    "    },\n" +
                    "    \"responseElements\": null,\n" +
                    "    \"additionalEventData\": {\n" +
                    "        \"SignatureVersion\": \"SigV4\",\n" +
                    "        \"CipherSuite\": \"ECDHE-RSA-AES128-GCM-SHA256\",\n" +
                    "        \"bytesTransferredIn\": 0,\n" +
                    "        \"AuthenticationMethod\": \"QueryString\",\n" +
                    "        \"x-amz-id-2\": \"m32YEFsDx1MNOzT3rJgNi1/S1qEtaYlVaAnznBlEa30C3/Amg4lTPW+XzXxPFgZ2cWIdwDZRbrc=\",\n" +
                    "        \"bytesTransferredOut\": 399801\n" +
                    "    },\n" +
                    "    \"requestID\": \"CE83452506E5C7E6\",\n" +
                    "    \"eventID\": \"e20f8c61-b550-4bed-aef3-a4a15775f15d\",\n" +
                    "    \"readOnly\": true,\n" +
                    "    \"resources\": [\n" +
                    "        {\n" +
                    "            \"type\": \"AWS::S3::Object\",\n" +
                    "            \"ARN\": \"arn:aws:s3:::guardiumdatalogig/Screen Shot 2020-05-31 at 15.48.34.png\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"accountId\": \"987076625343\",\n" +
                    "            \"type\": \"AWS::S3::Bucket\",\n" +
                    "            \"ARN\": \"arn:aws:s3:::guardiumdatalogig\"\n" +
                    "        }\n" +
                    "    ],\n" +
                    "    \"eventType\": \"AwsApiCall\",\n" +
                    "    \"managementEvent\": false,\n" +
                    "    \"recipientAccountId\": \"987076625343\",\n" +
                    "    \"eventCategory\": \"Data\"\n" +
                    "}";

    public static Record buildRecord(final JsonObject eventData) throws ParseException {

        log.debug("-1");
        if (eventData==null || eventData.get("eventName")==null){
            return null;
        }
        log.debug("0");
        // TODO get param.args.lsid, or fabricate
        Record record = new Record();

        JsonObject userIdentity = eventData.get("userIdentity")!=null ? eventData.get("userIdentity").getAsJsonObject() : null;

        log.debug("1");

        String accessKeyId = getValue(userIdentity,"accessKeyId");
        record.setSessionId(accessKeyId);

        log.debug("2");

        //String accountId = userIdentity.get("accountId").getAsString();
        JsonObject requestParameters = eventData.get("requestParameters")!=null ? eventData.get("requestParameters").getAsJsonObject() : null;
        String bucketName = getValue(requestParameters,"bucketName");
        record.setDbName(bucketName);

        log.debug("3");
        String arn = getValue(userIdentity,"arn");
        String userName = getValue(userIdentity,"userName");
        record.setAppUserName(arn);

        log.debug("4");
        String time = getValue(eventData,"eventTime");
        int unixTime = Parser.getTimeSeconds(time);
        record.setTime(unixTime);

        log.debug("5");
        SessionLocator sessionLocator = new SessionLocator();
        String sourceIPAddress = getValue(eventData,"sourceIPAddress");
        sessionLocator.setClientIp(sourceIPAddress);
        sessionLocator.setClientPort(0);

        log.debug("6");
        String host = getValue(requestParameters,"Host");
        String ipFromHost = "1.1.1.1";
        sessionLocator.setServerIp(ipFromHost);
        sessionLocator.setServerPort(0);

        log.debug("7");
        record.setSessionLocator(sessionLocator);

        Accessor accessor = new Accessor();
        accessor.setDbUser(userName);
        accessor.setServerType(S3_TYPE);
        accessor.setServerOs(UNKNOWN_STRING);

        log.debug("8");
        // parse userAgent field "[Mozilla/5.0 (Macintosh; Intel Mac OS X 10.14; rv:68.0) Gecko/20100101 Firefox/68.0]"
        String userAgent = getValue(eventData,"userAgent");
        int start = userAgent!=null ? userAgent.indexOf("("):-1;
        int end = userAgent!=null ? userAgent.indexOf(")", start):-1;

        log.debug("9");
        String clientOs = (start>=0 && end>0) ? userAgent.substring(start, end+1) : UNKNOWN_STRING;
        accessor.setClientOs(clientOs);

        log.debug("10");
        String eventSource = getValue(eventData,"eventSource");
        accessor.setClientHostName(eventSource/*sourceIPAddress*/);

        log.debug("11");
        accessor.setServerHostName(host);

        log.debug("12");
        String protocol = getValue(eventData,"eventType");
        accessor.setCommProtocol(protocol); // talk to Itai
        accessor.setDbProtocol(S3_TYPE);

        log.debug("13");
        String version = getValue(eventData,"eventVersion");
        accessor.setDbProtocolVersion(version);
        accessor.setOsUser(UNKNOWN_STRING);

        log.debug("14");
        accessor.setSourceProgram(eventSource);
        accessor.setClient_mac(UNKNOWN_STRING);
        accessor.setServerDescription(UNKNOWN_STRING);

        log.debug("15");
        end = eventSource.indexOf(".amazonaws.com");
        String serviceName = end > 0 ? eventSource.substring(0, end) : eventSource;
        accessor.setServiceName(serviceName);

        log.debug("16");
        accessor.setLanguage(UNKNOWN_STRING);

        log.debug("17");
        String eventType = getValue(eventData,"eventType");
        accessor.setType(eventType);

        log.debug("18");
        record.setAccessor(accessor);

        log.debug("19");
        String fullSql = eventData.toString();

        log.debug("20");
        Data data = new Data();
        record.setData(data);
        data.setTimestamp(unixTime);
        data.setOriginalSqlCommand(fullSql);
        data.setUseConstruct(true);

        log.debug("21");
        Construct construct = new Construct();
        data.setConstruct(construct);
        construct.setFull_sql(fullSql);
        construct.setOriginal_sql(fullSql);

        log.debug("22");
        ArrayList<Sentence> sentences = new ArrayList<>();
        construct.setSentences(sentences);

        log.debug("233");
        String eventName = getValue(eventData,"eventName");
        Sentence sentence = new Sentence(eventName); //verb
        sentences.add(sentence);

        ArrayList<SentenceObject> objects = new ArrayList<>();
        sentence.setObjects(objects);

        log.debug("23");
        String objectName = getValue(requestParameters, "key");
        SentenceObject object = new SentenceObject(objectName);
        objects.add(object);

       /*
        Sentence
        ArrayList<SentenceObject> objects = new ArrayList<>();
        */
        return record;
    }

    private static String getValue(JsonObject data, String property){
        String value = "N/A";
        if (data!=null){
            Object val = data.get(property);
            if (val!=null){
                value = ((JsonElement) val).getAsString();
            }
        }
        return value;
    }

    public static int getTimeSeconds(String dateString) throws ParseException {
        if (dateString==null){
            log.warn("DateString is null");
            return 0;
        }
        Date date = DATE_FORMATTER.parse(dateString);
        int timeSeconds = (int)(date.getTime() / 1000); 
        return timeSeconds;
    }

    public static void main(String[] args){
        //Parser parser = new Parser();
        try {
            String anotherEvent = "{\"eventID\":\"e3de78ee-4fab-40ef-b421-bfeff7f3c61c\",\"awsRegion\":\"us-east-1\",\"eventCategory\":\"Data\",\"eventVersion\":\"1.07\",\"responseElements\":null,\"sourceIPAddress\":\"77.125.48.244\",\"requestParameters\":{\"bucketName\":\"bucketnewbucketkkkkk\",\"X-Amz-Date\":\"20200622T202153Z\",\"response-content-disposition\":\"inline\",\"X-Amz-Algorithm\":\"AWS4-HMAC-SHA256\",\"X-Amz-SignedHeaders\":\"host\",\"Host\":\"bucketnewbucketkkkkk.s3.us-east-1.amazonaws.com\",\"X-Amz-Expires\":\"300\",\"key\":\"mkkkk/sampleJson.json\"},\"eventSource\":\"s3.amazonaws.com\",\"resources\":[{\"type\":\"AWS::S3::Object\",\"ARN\":\"arn:aws:s3:::bucketnewbucketkkkkk/mkkkk/sampleJson.json\"},{\"type\":\"AWS::S3::Bucket\",\"ARN\":\"arn:aws:s3:::bucketnewbucketkkkkk\",\"accountId\":\"987076625343\"}],\"readOnly\":true,\"userAgent\":\"[Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.97 Safari/537.36]\",\"userIdentity\":{\"sessionContext\":{\"attributes\":{\"mfaAuthenticated\":\"false\",\"creationDate\":\"2020-06-22T08:52:40Z\"}},\"accessKeyId\":\"ASIA6LUS2AO73E6D2NJP\",\"accountId\":\"987076625343\",\"principalId\":\"AIDAJWW2XAIOY2WN3KAAM\",\"arn\":\"arn:aws:iam::987076625343:user/ProxyTest\",\"type\":\"IAMUser\",\"userName\":\"ProxyTest\"},\"eventType\":\"AwsApiCall\",\"additionalEventData\":{\"SignatureVersion\":\"SigV4\",\"AuthenticationMethod\":\"QueryString\",\"x-amz-id-2\":\"CcbPzGZRlYbGdJPjFp8IcxuVi7ZhB01wkVVwhjkRDFbUDdFB+GKsV5oRannp3oOt2qCMUEXy37I\\u003d\",\"CipherSuite\":\"ECDHE-RSA-AES128-GCM-SHA256\",\"bytesTransferredOut\":19.0,\"bytesTransferredIn\":0.0},\"requestID\":\"4D5B1CD23AEDD7C7\",\"eventTime\":\"2020-06-22T20:21:53Z\",\"eventName\":\"GetObject\",\"recipientAccountId\":\"987076625343\",\"managementEvent\":false}";
            //JsonObject inputJSON = (JsonObject) JsonParser.parseString(GETOBJECT_EVENT);
            JsonObject inputJSON = (JsonObject) JsonParser.parseString(anotherEvent);
            Record record = Parser.buildRecord(inputJSON);
            System.out.println(record);
            String recordStr = gson.toJson(record);
            System.out.println(recordStr);
        } catch (Exception e){
            log.error("Failed to parse", e);
            System.out.println(e);
            e.printStackTrace();
        }
    }
}