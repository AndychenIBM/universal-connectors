package com.ibm.guardium.s3;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.google.gson.*;
import com.ibm.guardium.universalconnector.common.structures.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Parser {

    private static Log log = LogFactory.getLog(Parser.class);

    private static final String DATE_FORMAT_ISO = "yyyy-MM-dd'T'HH:mm:ss";// "yyyy-MM-dd'T'HH:mm:ssZ";
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(DATE_FORMAT_ISO);
    public static final String UNKNOWN_STRING = "N/A";
    public static final String BUCKETNAME_PROPERTY = "bucketName";
    public static final String BUCKET_PROPERTY = "Bucket";
    public static final String S3_TYPE = "S3";
    private static final Gson   gson = new Gson();


    public static Record buildRecord(JsonElement auditEl) throws Exception {

        if (auditEl==null || !auditEl.isJsonObject()){
            throw new Exception("Invalid event data");
        }

        Record record = new Record();

        // ------------------ Build record upper layer
        // userIdentity is a mandatory property
        JsonObject auditObj = (JsonObject)auditEl;
        JsonObject userIdentity = auditObj.get("userIdentity").getAsJsonObject();
        record.setSessionId(userIdentity.toString());

        JsonObject requestParameters = auditObj.get("requestParameters")!=null ? auditObj.get("requestParameters").getAsJsonObject() : null;
        String bucketName = searchForBucketName(requestParameters);
        if (bucketName!=null) {
            record.setDbName(bucketName);
        } else {
            record.setDbName(UNKNOWN_STRING);
        }

        String userName = searchForAppUserName(auditObj);
        record.setAppUserName(userName);

        String time = getStrValue(auditObj,"eventTime");
        long unixTime = getTimestamp(time);
        record.setTime(unixTime);

        // ------------------ Build Session locator
        SessionLocator sessionLocator = new SessionLocator();
        String sourceIPAddress = resolveHostToIP(getStrValue(auditObj,"sourceIPAddress"));
        sessionLocator.setClientIp(sourceIPAddress);
        sessionLocator.setClientPort(0);

        String host = getHost(requestParameters);
        String serverIP = resolveHostToIP(host);
        sessionLocator.setServerIp(serverIP);
        sessionLocator.setServerPort(0);

        record.setSessionLocator(sessionLocator);

        // ------------------ Build Accessor
        Accessor accessor = new Accessor();
        accessor.setDbUser(userName);
        accessor.setServerType(S3_TYPE);
        accessor.setServerOs(UNKNOWN_STRING);

        accessor.setClientHostName(getStrValue(auditObj,"sourceIPAddress"));
        accessor.setServerHostName(getStrValue(auditObj,"eventSource"));

        log.debug("12");
        String protocol = getStrValue(auditObj,"eventType");
        accessor.setCommProtocol(protocol); // talk to Itai
        accessor.setDbProtocol(S3_TYPE);

        log.debug("13");
        String version = getStrValue(auditObj,"eventVersion");
        accessor.setDbProtocolVersion(version);
        accessor.setOsUser(UNKNOWN_STRING);

        log.debug("14");
        //"userAgent": "aws-cli/1.10.32 Python/2.7.9 Windows/7 botocore/1.4.22",
        //"userAgent": "[Mozilla/5.0 (Macintosh; Intel Mac OS X 10.14; rv:68.0) Gecko/20100101 Firefox/68.0]"
        //"userAgent": "[S3Console/0.4, aws-internal/3 aws-sdk-java/1.11.783 Linux/4.9.217-0.1.ac.205.84.332.metal1.x86_64 OpenJDK_64-Bit_Server_VM/25.252-b09 java/1.8.0_252 vendor/Oracle_Corporation]"
        //"userAgent": "s3.amazonaws.com"
        // parse userAgent field "[Mozilla/5.0 (Macintosh; Intel Mac OS X 10.14; rv:68.0) Gecko/20100101 Firefox/68.0]"
        String userAgent = getStrValue(auditObj,"userAgent");
        if (userAgent!=null) {
            // source program
            int start = userAgent.indexOf("[") > 0 ? userAgent.indexOf("[") : 0;
            int end = userAgent.indexOf("/") > 0 ? userAgent.indexOf("/") : userAgent.length();
            String eventSource = userAgent.substring(start, end);
            if (eventSource != null) {
                accessor.setSourceProgram(eventSource);
            }

            // client os
            start = userAgent != null ? userAgent.indexOf("(") : -1;
            end = userAgent != null ? userAgent.indexOf(")", start) : -1;
            if (start >= 0 && end > 0){
                accessor.setClientOs(userAgent.substring(start, end + 1));
            }
        }

        accessor.setServiceName(getStrValue(auditObj,"eventSource"));

        log.debug("16");
        accessor.setLanguage(UNKNOWN_STRING);

        log.debug("17");
        String eventType = getStrValue(auditObj,"eventType");
        accessor.setType(eventType);

        log.debug("18");
        record.setAccessor(accessor);

        log.debug("19");
        // ------------------ Build data or Exception
        String errorCode = getStrValue(auditObj,"eventType");
        if (!UNKNOWN_STRING.equalsIgnoreCase(errorCode)){
            ExceptionRecord exceptionRecord = new ExceptionRecord();
            exceptionRecord.setExceptionTypeId(errorCode);
            String desc = getStrValue(auditObj,"errorMessage");
            if (!UNKNOWN_STRING.equalsIgnoreCase(desc)) {
                exceptionRecord.setDescription(desc);
            }
            exceptionRecord.setSqlString(auditObj.toString());
            record.setException(exceptionRecord);
        } else {
            log.debug("20");
            Data data = new Data();
            record.setData(data);
            String fullSql = auditObj.toString();
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
            String eventName = getStrValue(auditObj, "eventName");
            Sentence sentence = new Sentence(eventName); //verb
            sentences.add(sentence);

            ArrayList<SentenceObject> objects = new ArrayList<>();
            sentence.setObjects(objects);

            // if there are resources - treat each one of them as object and add to the data
            String resourcesStr = getStrValue(auditObj, "resources");
            try {
                if (!"N/A".equals(resourcesStr) && resourcesStr != null && resourcesStr.length() > 0) {

                    log.debug("resourcesStr is " + resourcesStr);
                    resourcesStr = gson.toJson(auditObj.get("resources"));
                    log.debug("resourcesStr AS JSON STR3 " + resourcesStr);

                    JsonElement resourcesJSON = JsonParser.parseString(resourcesStr);
                    if (resourcesJSON.isJsonArray()) {
                        JsonArray resourcesArr = (JsonArray) resourcesJSON;
                        for (int i = 0; i < resourcesArr.size(); i++) {
                            try {
                                SentenceObject resourceToObject = parseResourceToObject((JsonObject) resourcesArr.get(i));
                                objects.add(resourceToObject);
                            } catch (Exception e) {
                                log.error("Failed to parse resource to object, resource str is " + resourcesArr.get(i).getAsString(), e);
                            }
                        }
                    } else if (resourcesJSON.isJsonObject()) {
                        try {
                            SentenceObject resourceToObject = parseResourceToObject((JsonObject) resourcesJSON);
                            objects.add(resourceToObject);
                        } catch (Exception e) {
                            log.error("Failed to parse resource to object, resource str is " + resourcesJSON.getAsString(), e);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Failed to find resources for event type " + eventType + ", resourcesStr " + resourcesStr, e);
            }

            // if not found object in resources property - use the value in "key" field of request parameters
            if (objects.size() == 0) {
                log.debug("23");
                String objectName = getStrValue(requestParameters, "key");
                SentenceObject object = new SentenceObject(objectName);
                objects.add(object);
            }
        }

        return record;
    }

    private static String searchForBucketName(JsonElement data) {

        if (data==null){
            return null;

        } else if (data.isJsonPrimitive()){
            return null;

        } else if (data.isJsonArray()){
            // scan each arr object for bucket, use first bucket found
            JsonArray array = data.getAsJsonArray();
            String bucketName = null;
            for (JsonElement jsonElement : array) {
                bucketName = searchForBucketName(jsonElement);
                if (bucketName!=null){
                    return bucketName;
                }
            }

        } else if (data.isJsonObject()){
            // scan object properties for bucket
            JsonObject jsonObject = data.getAsJsonObject();
            JsonElement bucketNameEl = jsonObject.get(BUCKETNAME_PROPERTY);
            if (bucketNameEl!=null){
                return bucketNameEl.getAsString();
            }
            bucketNameEl = jsonObject.get(BUCKET_PROPERTY);
            if (bucketNameEl!=null){
                return bucketNameEl.getAsString();
            }

            // no direct bucket property - need to inner objects
            Set<String> properties = jsonObject.keySet();
            for (String property : properties) {
                String bucketName = searchForBucketName(jsonObject.get(property));
                if (bucketName!=null){
                    return bucketName;
                }
            }
        }
        // At this point - nothing was found, so just return null
        return null;
    }

    public static String searchForAppUserName(JsonObject eventData) {
        //https://docs.aws.amazon.com/awscloudtrail/latest/userguide/cloudtrail-event-reference-user-identity.html#cloudtrail-event-reference-user-identity-examples
        //maybe better use the link below?
        //https://docs.aws.amazon.com/macie/latest/userguide/macie-users.html
        JsonObject userIdentity = eventData.get("userIdentity").getAsJsonObject();
        String type = getStrValue(userIdentity, "type");
        String userName = null;
        switch (type){
            case "Root":
            case "IAMUser":
                userName = getStrValue(userIdentity,"userName");
                break;
            case "AssumedRole":
            case "FederatedUser":
                JsonObject sessionContext = userIdentity.get("sessionContext").getAsJsonObject();
                JsonObject sessionIssuer = sessionContext.get("sessionIssuer").getAsJsonObject();
                String sessionIssuerType = getStrValue( sessionIssuer, "type ");
                if ("Root".equals(sessionIssuerType)){
                    userName = "Root sessionIssuer type";
                } else {
                    userName = getStrValue(sessionIssuer, "userName");
                }
                break;
            case "AWSAccount":
                userName = "AWSService";
            case "AWSService":
                userName = "AWSService";
        }
        return userName;
    }

    public static String getHost(JsonObject requestParameters){
        String[] properties = {"Host", "host"};
        for (String property : properties) {
            JsonElement val = requestParameters.get(property);
            if (val != null) {
                if (val.isJsonPrimitive()) {
                    return val.getAsString();
                } else if (val.isJsonArray()){
                    return val.getAsJsonArray().get(0).getAsString();
                }
            }
        }
        return null;
    }

    public static String resolveHostToIP(String sourceIPAddress){
        try {
            InetAddress address = InetAddress.getByName(sourceIPAddress);
            return address.getHostAddress();
        }catch (UnknownHostException e){
            log.error("Falied to translate the sourceIPAdress "+sourceIPAddress+" to ip, sending original string", e);
        }
        return sourceIPAddress;
    }

    /*
    * {\n" +
                    "            \"accountId\": \"987076625343\",\n" +
                    "            \"type\": \"AWS::S3::Bucket\",\n" +
                    "            \"ARN\": \"arn:aws:s3:::guardiumdatalogig\"\n" +
                    "        }\n"
    * */
    private static SentenceObject parseResourceToObject(JsonObject jsonObj) {
        String arn = getStrValue(jsonObj, "ARN");
        String name = extractLastPartOfString(arn, /*":::"*/":");
        SentenceObject obj = new SentenceObject(name);

        String typeStr = getStrValue(jsonObj, "type");
        String type = extractLastPartOfString(typeStr, /*"::"*/":");
        obj.type = type;

        return obj;
    }

    private static String extractLastPartOfString(String orig, String delimiter){
        String[] parts = orig.split(delimiter);
        String part = parts.length>0 ? parts[parts.length-1]:null;
        return part;
    }

    private static String getStrValue(JsonObject data, String[] properties){
        if (properties==null){
            return null;
        }
        for (String property : properties) {
            String val = getStrValue(data, property);
            if (!UNKNOWN_STRING.equalsIgnoreCase(val)){
                return val;
            }
        }
        return UNKNOWN_STRING;
    }

    private static String getStrValue(JsonObject data, String property){
        String value = UNKNOWN_STRING;
        if (data!=null){
            JsonElement val = data.get(property);
            if (val!=null){
                if (val.isJsonPrimitive()) {
                    value = val.getAsString();
                } else {
                    value = val.toString();
                }
            }
        }
        return value;
    }

    public static long getTimestamp(String dateString) throws ParseException {
        if (dateString==null){
            log.warn("DateString is null");
            return 0;
        }
        Date date = DATE_FORMATTER.parse(dateString);
        return date.getTime();
    }

    public static void main(String[] args){
        //Parser parser = new Parser();
        try {
            String anotherEvent = "{\"eventID\":\"e3de78ee-4fab-40ef-b421-bfeff7f3c61c\",\"awsRegion\":\"us-east-1\",\"eventCategory\":\"Data\",\"eventVersion\":\"1.07\",\"responseElements\":null,\"sourceIPAddress\":\"77.125.48.244\",\"requestParameters\":{\"bucketName\":\"bucketnewbucketkkkkk\",\"X-Amz-Date\":\"20200622T202153Z\",\"response-content-disposition\":\"inline\",\"X-Amz-Algorithm\":\"AWS4-HMAC-SHA256\",\"X-Amz-SignedHeaders\":\"host\",\"Host\":\"bucketnewbucketkkkkk.s3.us-east-1.amazonaws.com\",\"X-Amz-Expires\":\"300\",\"key\":\"mkkkk/sampleJson.json\"},\"eventSource\":\"s3.amazonaws.com\",\"resources\":[{\"type\":\"AWS::S3::Object\",\"ARN\":\"arn:aws:s3:::bucketnewbucketkkkkk/mkkkk/sampleJson.json\"},{\"type\":\"AWS::S3::Bucket\",\"ARN\":\"arn:aws:s3:::bucketnewbucketkkkkk\",\"accountId\":\"987076625343\"}],\"readOnly\":true,\"userAgent\":\"[Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.97 Safari/537.36]\",\"userIdentity\":{\"sessionContext\":{\"attributes\":{\"mfaAuthenticated\":\"false\",\"creationDate\":\"2020-06-22T08:52:40Z\"}},\"accessKeyId\":\"ASIA6LUS2AO73E6D2NJP\",\"accountId\":\"987076625343\",\"principalId\":\"AIDAJWW2XAIOY2WN3KAAM\",\"arn\":\"arn:aws:iam::987076625343:user/ProxyTest\",\"type\":\"IAMUser\",\"userName\":\"ProxyTest\"},\"eventType\":\"AwsApiCall\",\"additionalEventData\":{\"SignatureVersion\":\"SigV4\",\"AuthenticationMethod\":\"QueryString\",\"x-amz-id-2\":\"CcbPzGZRlYbGdJPjFp8IcxuVi7ZhB01wkVVwhjkRDFbUDdFB+GKsV5oRannp3oOt2qCMUEXy37I\\u003d\",\"CipherSuite\":\"ECDHE-RSA-AES128-GCM-SHA256\",\"bytesTransferredOut\":19.0,\"bytesTransferredIn\":0.0},\"requestID\":\"4D5B1CD23AEDD7C7\",\"eventTime\":\"2020-06-22T20:21:53Z\",\"eventName\":\"GetObject\",\"recipientAccountId\":\"987076625343\",\"managementEvent\":false}";
            JsonObject inputJSON = (JsonObject) JsonParser.parseString(anotherEvent);

//            JsonObject inputJSON = (JsonObject) JsonParser.parseString(EventSamples.getSamplesByEventName(EventSamples.EventName.DeleteBucket).get(0).getJsonStr());
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