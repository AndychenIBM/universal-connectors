package com.ibm.guardium.universalconnector.transformer;


import com.google.gson.Gson;
import com.ibm.guardium.proto.datasource.Datasource;
import com.ibm.guardium.universalconnector.commons.structures.*;
import com.ibm.guardium.universalconnector.exceptions.GuardUCInvalidRecordException;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class JsonRecordTransformerTest {
    private static String recordString = "{\n" +
            "\t\t\"sessionId\":\"n/a\",\n" +
            "\t\t\"dbName\":\"students\",\n" +
            "\t\t\"appUserName\":\"n/a\",\n" +
            "\t\t\"time\":{ \"timstamp\": 1581841318, \"minOffsetFromGMT\":2, \"minDst\":0 },\n" +
            "\t\t\"sessionLocator\":{\n" +
            "\t\t\t\"clientIp\":\"127.0.20.1\",\n" +
            "\t\t\t\"clientPort\":1234,\n" +
            "\t\t\t\"serverIp\":\"127.0.30.1\",\n" +
            "\t\t\t\"serverPort\":5678,\n" +
            "\t\t\t\"isIpv6\":false,\n" +
            "\t\t\t\"clientIpv6\":\"n/a\",\n" +
            "\t\t\t\"serverIpv6\":\"n/a\"\n" +
            "\t\t},\n" +
            "\t\t\"accessor\":{\n" +
            "\t\t\t\"dbUser\":\"admin\",\n" +
            "\t\t\t\"serverType\":\"MONGODB\",\n" +
            "\t\t\t\"serverOs\":\"n/a\",\n" +
            "\t\t\t\"clientOs\":\"n/a\",\n" +
            "\t\t\t\"clientHostName\":\"n/a\",\n" +
            "\t\t\t\"serverHostName\":\"qa-db51\",\n" +
            "\t\t\t\"commProtocol\":\"n/a\",\n" +
            "\t\t\t\"dbProtocol\":\"Logstash\",\n" +
            "\t\t\t\"dbProtocolVersion\":\"n/a\",\n" +
            "\t\t\t\"osUser\":\"n/a\",\n" +
            "\t\t\t\"sourceProgram\":\"mongod\",\n" +
            "\t\t\t\"client_mac\":\"n/a\",\n" +
            "\t\t\t\"serverDescription\":\"n/a\",\n" +
            "\t\t\t\"serviceName\":\"abcservicename\",\n" +
            "\t\t\t\"language\":\"FREE_TEXT\",\n" +
            "\t\t\t\"dataType\":\"CONSTRUCT\"\n" +
            "\t\t},\n" +
            "\t\t\"data\":{\n" +
            "\t\t\t\"timestamp\":0,\n" +
            "\t\t\t\"useConstruct\":true,\n" +
            "\t\t\t\"originalSqlCommand\":\"\",\n" +
            "\t\t\t\"construct\":{\n" +
            "\t\t\t\t\"sentences\":[\n" +
            "\t\t\t\t\t{\n" +
            "\t\t\t\t\t\t\"verb\":\"find\",\n" +
            "\t\t\t\t\t\t\"objects\":[{\"name\":\"transactions\",\"type\":\"collection\",\"fields\":[],\"schema\":\"\"}],\n" +
            "\t\t\t\t\t\t\"descendants\":[],\n" +
            "\t\t\t\t\t\t\"fields\":[]\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t],\n" +
            "\t\t\t\t\"fullSql\":\" { \\\"atype\\\" : \\\"authCheck\\\", \\\"ts\\\" : { \\\"$date\\\" : \\\"2020-02-16T03:21:58.185-0500\\\" }, \\\"local\\\" : { \\\"ip\\\" : \\\"127.0.30.1\\\", \\\"port\\\" : 0 }, \\\"remote\\\" : { \\\"ip\\\" : \\\"127.0.20.1\\\", \\\"port\\\" : 0 }, \\\"users\\\" : [], \\\"roles\\\" : [], \\\"param\\\" : { \\\"command\\\" : \\\"find\\\", \\\"ns\\\" : \\\"config.transactions\\\", \\\"args\\\" : { \\\"find\\\" : \\\"transactions\\\", \\\"filter\\\" : { \\\"lastWriteDate\\\" : { \\\"$lt\\\" : { \\\"$date\\\" : \\\"2020-02-16T02:51:58.185-0500\\\" } } }, \\\"projection\\\" : { \\\"_id\\\" : 1 }, \\\"sort\\\" : { \\\"_id\\\" : 1 }, \\\"$db\\\" : \\\"config\\\" } }, \\\"result\\\" : 0 }\"\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t}\n";

    private static String recordStringTemplate= "{\n" +
            "\t\t\"sessionId\":\"n/a\",\n" +
            "\t\t\"dbName\":\"students\",\n" +
            "\t\t\"appUserName\":\"n/a\",\n" +
            "\t\t\"time\":{ \"timstamp\": 1581841318, \"minOffsetFromGMT\":2, \"minDst\":0 },\n" +
            "\t\t\"sessionLocator\":{\n" +
            "\t\t\t\"clientIp\":\"127.0.20.1\",\n" +
            "\t\t\t\"clientPort\":1234,\n" +
            "\t\t\t\"serverIp\":\"127.0.30.1\",\n" +
            "\t\t\t\"serverPort\":5678,\n" +
            "\t\t\t\"isIpv6\":false,\n" +
            "\t\t\t\"clientIpv6\":\"n/a\",\n" +
            "\t\t\t\"serverIpv6\":\"n/a\"\n" +
            "\t\t},\n" +
            "\t\t\"accessor\":{\n" +
            "\t\t\t\"dbUser\":\"admin\",\n" +
            "\t\t\t\"serverType\":\"MONGODB\",\n" +
            "\t\t\t\"serverOs\":\"n/a\",\n" +
            "\t\t\t\"clientOs\":\"n/a\",\n" +
            "\t\t\t\"clientHostName\":\"n/a\",\n" +
            "\t\t\t\"serverHostName\":\"qa-db51\",\n" +
            "\t\t\t\"commProtocol\":\"n/a\",\n" +
            "\t\t\t\"dbProtocol\":\"Logstash\",\n" +
            "\t\t\t\"dbProtocolVersion\":\"n/a\",\n" +
            "\t\t\t\"osUser\":\"n/a\",\n" +
            "\t\t\t\"sourceProgram\":\"mongod\",\n" +
            "\t\t\t\"client_mac\":\"n/a\",\n" +
            "\t\t\t\"serverDescription\":\"n/a\",\n" +
            "\t\t\t\"serviceName\":\"abcservicename\",\n" +
            "\t\t\t\"language\":\"FREE_TEXT\",\n" +
            "\t\t\t\"dataType\":\"%s\"\n" +
            "\t\t},\n" +
            "\t\t\"data\":{\n" +
            "\t\t\t\"timestamp\":0,\n" +
            "\t\t\t\"useConstruct\":true,\n" +
            "\t\t\t\"originalSqlCommand\":\"\",\n" +
            "\t\t\t\"construct\":{\n" +
            "\t\t\t\t\"sentences\":[\n" +
            "\t\t\t\t\t{\n" +
            "\t\t\t\t\t\t\"verb\":\"find\",\n" +
            "\t\t\t\t\t\t\"objects\":[{\"name\":\"transactions\",\"type\":\"collection\",\"fields\":[],\"schema\":\"\"}],\n" +
            "\t\t\t\t\t\t\"descendants\":[],\n" +
            "\t\t\t\t\t\t\"fields\":[]\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t],\n" +
            "\t\t\t\t\"fullSql\":\" { \\\"atype\\\" : \\\"authCheck\\\", \\\"ts\\\" : { \\\"$date\\\" : \\\"2020-02-16T03:21:58.185-0500\\\" }, \\\"local\\\" : { \\\"ip\\\" : \\\"127.0.30.1\\\", \\\"port\\\" : 0 }, \\\"remote\\\" : { \\\"ip\\\" : \\\"127.0.20.1\\\", \\\"port\\\" : 0 }, \\\"users\\\" : [], \\\"roles\\\" : [], \\\"param\\\" : { \\\"command\\\" : \\\"find\\\", \\\"ns\\\" : \\\"config.transactions\\\", \\\"args\\\" : { \\\"find\\\" : \\\"transactions\\\", \\\"filter\\\" : { \\\"lastWriteDate\\\" : { \\\"$lt\\\" : { \\\"$date\\\" : \\\"2020-02-16T02:51:58.185-0500\\\" } } }, \\\"projection\\\" : { \\\"_id\\\" : 1 }, \\\"sort\\\" : { \\\"_id\\\" : 1 }, \\\"$db\\\" : \\\"config\\\" } }, \\\"result\\\" : 0 }\"\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t}\n";

    @Test
    public void testParsingRecordToJson(){

        Record record = (new Gson()).fromJson(recordString, Record.class);

        Assert.assertTrue("failed to parse record string to json", record!=null);
    }

    @Test
    public void testRecordTrasformation(){

        JsonRecordTransformer transformer = new JsonRecordTransformer();

        List<Datasource.Guard_ds_message> messages = transformer.transform(recordString);
        Datasource.Session_start session_start = messages.get(0).getSessionStart();

    }

    @Test
    public void testSessionLocatorCreation(){
        JsonRecordTransformer transformer = new JsonRecordTransformer();
        Record record = new Gson().fromJson(recordString, Record.class);
        Datasource.Session_locator   sessionLocator= transformer.buildSessionLocator(record);
        Assert.assertTrue("invalid client ip", sessionLocator.getClientIp()==18088063);
        Assert.assertTrue("invalid client port", sessionLocator.getClientPort()==1234);
        Assert.assertTrue("invalid server ip", sessionLocator.getServerIp()==18743423);
        Assert.assertTrue("invalid server port", sessionLocator.getServerPort()==5678);
    }

    @Test
    public void testAccessorCreation(){
        JsonRecordTransformer transformer = new JsonRecordTransformer();
        // standard "no special cases" test
        Record record = new Gson().fromJson(recordString, Record.class);
        Accessor ra = record.getAccessor();
        Datasource.Accessor accessor = transformer.buildAccessor(record);
        Assert.assertTrue("invalid dbUser", accessor.getDbUser().equals("admin"));
        Assert.assertTrue("invalid serverType", accessor.getServerType().equals("MONGODB"));
        Assert.assertTrue("invalid serverOs", accessor.getServerOs().equals(ra.getServerOs()));
        Assert.assertTrue("invalid serverOs", accessor.getClientHostname().equals(ra.getClientHostName()));
        Assert.assertTrue("invalid serverHostname", accessor.getServerHostname().equals(ra.getServerHostName()));
        Assert.assertTrue("invalid commProtocol", accessor.getCommProtocol().equals(ra.getCommProtocol()));
        Assert.assertTrue("invalid DbProtocol", accessor.getDbProtocol().substring(JsonRecordTransformer.UC_PROTOCOL_PREFIX.length()).equals(ra.getDbProtocol()));
        Assert.assertTrue("invalid DbProtocolVersion", accessor.getDbProtocolVersion().equals(ra.getDbProtocolVersion()));
        Assert.assertTrue("invalid sourceProgram", accessor.getSourceProgram().equals(ra.getSourceProgram()));
        Assert.assertTrue("invalid serverDescription", accessor.getServerDescription().equals(ra.getServerDescription()));
        Assert.assertTrue("invalid serviceName", accessor.getServiceName().equals(ra.getServiceName()));
        Assert.assertTrue("invalid language", accessor.getLanguage().equals(Datasource.Application_data.Language_type.valueOf(ra.getLanguage().toUpperCase())));
        Assert.assertTrue("invalid dataType", accessor.getType().equals(Datasource.Application_data.Data_type.CONSTRUCT));
        Assert.assertTrue("invalid datasourceType", accessor.getDatasourceType().equals(Datasource.Application_data.Datasource_type.UNI_CON));


        // empty fields test
        String accessorString ="{\n" +
                "\t\t\t\"dbUser\":\"admin\",\n" +
                "\t\t\t\"serverType\":\"MONGODB\",\n" +
                "\t\t\t\"clientOs\":\"n/a\",\n" +
                "\t\t\t\"clientHostName\":\"n/a\",\n" +
                "\t\t\t\"serverHostName\":\"qa-db51\",\n" +
                "\t\t\t\"commProtocol\":\"n/a\",\n" +
                "\t\t\t\"dbProtocol\":\"Logstash\",\n" +
                "\t\t\t\"dbProtocolVersion\":\"n/a\",\n" +
                "\t\t\t\"osUser\":\"n/a\",\n" +
                "\t\t\t\"sourceProgram\":\"mongod\",\n" +
                "\t\t\t\"client_mac\":\"n/a\",\n" +
                "\t\t\t\"serverDescription\":\"n/a\",\n" +
                "\t\t\t\"serviceName\":\"abcservicename\",\n" +
                "\t\t\t\"language\":\"FREE_TEXT\",\n" +
                "\t\t\t\"dataType\":\"CONSTRUCT\"\n" +
                "\t\t}";
        ra = new Gson().fromJson(accessorString, Accessor.class);
        record = new Record();
        record.setAccessor(ra);
    }


    @Test/*(expected = GuardUCInvalidRecordException.class)*/
    public void testAccessorInvalidDataTypeHandling(){
        JsonRecordTransformer transformer = new JsonRecordTransformer();

        // test CONSTRUCT ( this datatype means that guardium should NOT parse msg, but should take construct data object)
        String recordWithDataTypeConstruct = String.format(recordStringTemplate, "CONSTRUCT");
        Record record = new Gson().fromJson(recordWithDataTypeConstruct, Record.class);
        Datasource.Accessor accessor = transformer.buildAccessor(record);
        Assert.assertTrue("Data type is not CONSTRUCT", Datasource.Application_data.Data_type.CONSTRUCT.equals(accessor.getType()));

        // test TEXT ( this datatype means that guardium should parse msg )
        String recordWithDataTypeText = String.format(recordStringTemplate, "TEXT");
        record = new Gson().fromJson(recordWithDataTypeText, Record.class);
        accessor = transformer.buildAccessor(record);
        Assert.assertTrue("Data type is not TEXT", Datasource.Application_data.Data_type.TEXT.equals(accessor.getType()));


        String recordWithDataTypeInvalid = String.format(recordStringTemplate, "TEXT111");
        record = new Gson().fromJson(recordWithDataTypeInvalid, Record.class);
        try {
            transformer.buildAccessor(record);
        } catch (GuardUCInvalidRecordException e) {
            Assert.assertTrue("Invalid exception text "+e.getMessage(), "Invalid Accessor data type: TEXT111".equals(e.getMessage()));
        }
    }

//    @Rule
//    public ExpectedException exceptionRule = ExpectedException.none();
//
//    @Test/*(expected = GuardUCInvalidRecordException.class)*/
//    public void testAccessorInvalidDataTypeHandling(){
//        //ExpectedException exceptionRule = ExpectedException.none();
//        exceptionRule.expect(GuardUCInvalidRecordException.class);
//        exceptionRule.expectMessage("Invalid Accessor data type: TEXT111");
//
//        JsonRecordTransformer transformer = new JsonRecordTransformer();
//        String recordWithDataTypeInvalid = String.format(recordStringTemplate, "TEXT111");
//        Record record = new Gson().fromJson(recordWithDataTypeInvalid, Record.class);
//        transformer.buildAccessor(record);
//    }

    @Test
    public void testS3Message(){
        String msgS3 = "{\"sessionId\":\"ASIA6LUS2AO7VLJQDY3Y\",\"dbName\":\"bucketnewbucketkkkkk\",\"appUserName\":\"arn:aws:iam::987076625343:user/ProxyTest\"," +
                "\"time\":{ \"timstamp\": 1581841318, \"minOffsetFromGMT\":2, \"minDst\":0 }," +
                "\"sessionLocator\":{\"clientIp\":\"77.125.48.244\",\"clientPort\":0,\"serverIp\":\"1.1.1.1\",\"serverPort\":0,\"isIpv6\":false,\"clientIpv6\":null,\"serverIpv6\":null}," +
                "\"accessor\":{\"dbUser\":\"ProxyTest\",\"serverType\":\"S3\",\"serverOs\":\"N/A\",\"clientOs\":\"(Windows NT 10.0; Win64; x64)\",\"clientHostName\":\"s3.amazonaws.com\",\"serverHostName\":\"bucketnewbucketkkkkk.s3.us-east-1.amazonaws.com\",\"commProtocol\":\"AwsApiCall\",\"dbProtocol\":\"S3\",\"dbProtocolVersion\":\"1.07\",\"osUser\":\"N/A\",\"sourceProgram\":\"s3.amazonaws.com\",\"client_mac\":\"N/A\",\"serverDescription\":\"N/A\",\"serviceName\":\"s3\",\"language\":\"N/A\",\"dataType\":\"CONSTRUCT\"},\"data\":{\"construct\":{\"sentences\":[{\"verb\":\"GetObject\",\"objects\":[{\"name\":\"sampleJson.json\",\"type\":\"collection\",\"fields\":[],\"schema\":\"\"}],\"descendants\":[],\"fields\":[]}],\"fullSql\":\"{\\\"eventID\\\":\\\"510749cc-f696-4ef6-9750-67f6a1b6563c\\\",\\\"awsRegion\\\":\\\"us-east-1\\\",\\\"eventCategory\\\":\\\"Data\\\",\\\"responseElements\\\":null,\\\"eventVersion\\\":\\\"1.07\\\",\\\"sourceIPAddress\\\":\\\"77.125.48.244\\\",\\\"eventSource\\\":\\\"s3.amazonaws.com\\\",\\\"requestParameters\\\":{\\\"bucketName\\\":\\\"bucketnewbucketkkkkk\\\",\\\"X-Amz-Date\\\":\\\"20200623T101515Z\\\",\\\"response-content-disposition\\\":\\\"inline\\\",\\\"X-Amz-Algorithm\\\":\\\"AWS4-HMAC-SHA256\\\",\\\"X-Amz-SignedHeaders\\\":\\\"host\\\",\\\"Host\\\":\\\"bucketnewbucketkkkkk.s3.us-east-1.amazonaws.com\\\",\\\"X-Amz-Expires\\\":\\\"300\\\",\\\"key\\\":\\\"sampleJson.json\\\"},\\\"resources\\\":[{\\\"type\\\":\\\"AWS::S3::Object\\\",\\\"ARN\\\":\\\"arn:aws:s3:::bucketnewbucketkkkkk/sampleJson.json\\\"},{\\\"type\\\":\\\"AWS::S3::Bucket\\\",\\\"accountId\\\":\\\"987076625343\\\",\\\"ARN\\\":\\\"arn:aws:s3:::bucketnewbucketkkkkk\\\"}],\\\"readOnly\\\":true,\\\"userAgent\\\":\\\"[Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.97 Safari/537.36]\\\",\\\"userIdentity\\\":{\\\"accessKeyId\\\":\\\"ASIA6LUS2AO7VLJQDY3Y\\\",\\\"sessionContext\\\":{\\\"attributes\\\":{\\\"mfaAuthenticated\\\":\\\"false\\\",\\\"creationDate\\\":\\\"2020-06-23T10:15:01Z\\\"}},\\\"accountId\\\":\\\"987076625343\\\",\\\"principalId\\\":\\\"AIDAJWW2XAIOY2WN3KAAM\\\",\\\"arn\\\":\\\"arn:aws:iam::987076625343:user/ProxyTest\\\",\\\"type\\\":\\\"IAMUser\\\",\\\"userName\\\":\\\"ProxyTest\\\"},\\\"eventType\\\":\\\"AwsApiCall\\\",\\\"additionalEventData\\\":{\\\"SignatureVersion\\\":\\\"SigV4\\\",\\\"AuthenticationMethod\\\":\\\"QueryString\\\",\\\"x-amz-id-2\\\":\\\"iy0pfZO0Lt10k2rVbj2B3CXnIsuoaX+tWd2EXSrMj3lx820yhuEyvceMyIJw/46/g8Qu5oIww+Q\\u003d\\\",\\\"CipherSuite\\\":\\\"ECDHE-RSA-AES128-GCM-SHA256\\\",\\\"bytesTransferredOut\\\":19.0,\\\"bytesTransferredIn\\\":0.0},\\\"requestID\\\":\\\"994668EDEA5422DD\\\",\\\"eventTime\\\":\\\"2020-06-23T10:15:16Z\\\",\\\"recipientAccountId\\\":\\\"987076625343\\\",\\\"eventName\\\":\\\"GetObject\\\",\\\"managementEvent\\\":false}\",\"original_sql\":\"{\\\"eventID\\\":\\\"510749cc-f696-4ef6-9750-67f6a1b6563c\\\",\\\"awsRegion\\\":\\\"us-east-1\\\",\\\"eventCategory\\\":\\\"Data\\\",\\\"responseElements\\\":null,\\\"eventVersion\\\":\\\"1.07\\\",\\\"sourceIPAddress\\\":\\\"77.125.48.244\\\",\\\"eventSource\\\":\\\"s3.amazonaws.com\\\",\\\"requestParameters\\\":{\\\"bucketName\\\":\\\"bucketnewbucketkkkkk\\\",\\\"X-Amz-Date\\\":\\\"20200623T101515Z\\\",\\\"response-content-disposition\\\":\\\"inline\\\",\\\"X-Amz-Algorithm\\\":\\\"AWS4-HMAC-SHA256\\\",\\\"X-Amz-SignedHeaders\\\":\\\"host\\\",\\\"Host\\\":\\\"bucketnewbucketkkkkk.s3.us-east-1.amazonaws.com\\\",\\\"X-Amz-Expires\\\":\\\"300\\\",\\\"key\\\":\\\"sampleJson.json\\\"},\\\"resources\\\":[{\\\"type\\\":\\\"AWS::S3::Object\\\",\\\"ARN\\\":\\\"arn:aws:s3:::bucketnewbucketkkkkk/sampleJson.json\\\"},{\\\"type\\\":\\\"AWS::S3::Bucket\\\",\\\"accountId\\\":\\\"987076625343\\\",\\\"ARN\\\":\\\"arn:aws:s3:::bucketnewbucketkkkkk\\\"}],\\\"readOnly\\\":true,\\\"userAgent\\\":\\\"[Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.97 Safari/537.36]\\\",\\\"userIdentity\\\":{\\\"accessKeyId\\\":\\\"ASIA6LUS2AO7VLJQDY3Y\\\",\\\"sessionContext\\\":{\\\"attributes\\\":{\\\"mfaAuthenticated\\\":\\\"false\\\",\\\"creationDate\\\":\\\"2020-06-23T10:15:01Z\\\"}},\\\"accountId\\\":\\\"987076625343\\\",\\\"principalId\\\":\\\"AIDAJWW2XAIOY2WN3KAAM\\\",\\\"arn\\\":\\\"arn:aws:iam::987076625343:user/ProxyTest\\\",\\\"type\\\":\\\"IAMUser\\\",\\\"userName\\\":\\\"ProxyTest\\\"},\\\"eventType\\\":\\\"AwsApiCall\\\",\\\"additionalEventData\\\":{\\\"SignatureVersion\\\":\\\"SigV4\\\",\\\"AuthenticationMethod\\\":\\\"QueryString\\\",\\\"x-amz-id-2\\\":\\\"iy0pfZO0Lt10k2rVbj2B3CXnIsuoaX+tWd2EXSrMj3lx820yhuEyvceMyIJw/46/g8Qu5oIww+Q\\u003d\\\",\\\"CipherSuite\\\":\\\"ECDHE-RSA-AES128-GCM-SHA256\\\",\\\"bytesTransferredOut\\\":19.0,\\\"bytesTransferredIn\\\":0.0},\\\"requestID\\\":\\\"994668EDEA5422DD\\\",\\\"eventTime\\\":\\\"2020-06-23T10:15:16Z\\\",\\\"recipientAccountId\\\":\\\"987076625343\\\",\\\"eventName\\\":\\\"GetObject\\\",\\\"managementEvent\\\":false}\"},\"timestamp\":1592907316,\"originalSqlCommand\":\"{\\\"eventID\\\":\\\"510749cc-f696-4ef6-9750-67f6a1b6563c\\\",\\\"awsRegion\\\":\\\"us-east-1\\\",\\\"eventCategory\\\":\\\"Data\\\",\\\"responseElements\\\":null,\\\"eventVersion\\\":\\\"1.07\\\",\\\"sourceIPAddress\\\":\\\"77.125.48.244\\\",\\\"eventSource\\\":\\\"s3.amazonaws.com\\\",\\\"requestParameters\\\":{\\\"bucketName\\\":\\\"bucketnewbucketkkkkk\\\",\\\"X-Amz-Date\\\":\\\"20200623T101515Z\\\",\\\"response-content-disposition\\\":\\\"inline\\\",\\\"X-Amz-Algorithm\\\":\\\"AWS4-HMAC-SHA256\\\",\\\"X-Amz-SignedHeaders\\\":\\\"host\\\",\\\"Host\\\":\\\"bucketnewbucketkkkkk.s3.us-east-1.amazonaws.com\\\",\\\"X-Amz-Expires\\\":\\\"300\\\",\\\"key\\\":\\\"sampleJson.json\\\"},\\\"resources\\\":[{\\\"type\\\":\\\"AWS::S3::Object\\\",\\\"ARN\\\":\\\"arn:aws:s3:::bucketnewbucketkkkkk/sampleJson.json\\\"},{\\\"type\\\":\\\"AWS::S3::Bucket\\\",\\\"accountId\\\":\\\"987076625343\\\",\\\"ARN\\\":\\\"arn:aws:s3:::bucketnewbucketkkkkk\\\"}],\\\"readOnly\\\":true,\\\"userAgent\\\":\\\"[Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.97 Safari/537.36]\\\",\\\"userIdentity\\\":{\\\"accessKeyId\\\":\\\"ASIA6LUS2AO7VLJQDY3Y\\\",\\\"sessionContext\\\":{\\\"attributes\\\":{\\\"mfaAuthenticated\\\":\\\"false\\\",\\\"creationDate\\\":\\\"2020-06-23T10:15:01Z\\\"}},\\\"accountId\\\":\\\"987076625343\\\",\\\"principalId\\\":\\\"AIDAJWW2XAIOY2WN3KAAM\\\",\\\"arn\\\":\\\"arn:aws:iam::987076625343:user/ProxyTest\\\",\\\"type\\\":\\\"IAMUser\\\",\\\"userName\\\":\\\"ProxyTest\\\"},\\\"eventType\\\":\\\"AwsApiCall\\\",\\\"additionalEventData\\\":{\\\"SignatureVersion\\\":\\\"SigV4\\\",\\\"AuthenticationMethod\\\":\\\"QueryString\\\",\\\"x-amz-id-2\\\":\\\"iy0pfZO0Lt10k2rVbj2B3CXnIsuoaX+tWd2EXSrMj3lx820yhuEyvceMyIJw/46/g8Qu5oIww+Q\\u003d\\\",\\\"CipherSuite\\\":\\\"ECDHE-RSA-AES128-GCM-SHA256\\\",\\\"bytesTransferredOut\\\":19.0,\\\"bytesTransferredIn\\\":0.0},\\\"requestID\\\":\\\"994668EDEA5422DD\\\",\\\"eventTime\\\":\\\"2020-06-23T10:15:16Z\\\",\\\"recipientAccountId\\\":\\\"987076625343\\\",\\\"eventName\\\":\\\"GetObject\\\",\\\"managementEvent\\\":false}\",\"useConstruct\":true},\"exception\":null}";
        JsonRecordTransformer transformer = new JsonRecordTransformer();
        List<Datasource.Guard_ds_message> msgs = transformer.transform(msgS3);
    }

    @Test
    public void testTrimming() {

        Record record = (new Gson()).fromJson(msgTemplateIPV6, Record.class);
        Datasource.Accessor.Builder builder = Datasource.Accessor.newBuilder();

        //-------trimAndSet----------//
        // test null
        Accessor ra = record.getAccessor();
        builder = Datasource.Accessor.newBuilder();
        ra.setDbUser(null);
        try {
            JsonRecordTransformer.trimAndSet(builder::setDbUser, ra::getDbUser);
            Assert.assertTrue("Exception should have been thrown for null value in property value", false);
        } catch (Exception e){
            Assert.assertTrue("Invalid exception is thrown for null in property value", e.getClass().equals(NullPointerException.class));
        }

        // test empty string
        ra.setDbUser("  ");
        builder = Datasource.Accessor.newBuilder();
        JsonRecordTransformer.trimAndSet(builder::setDbUser, ra::getDbUser);
        Assert.assertTrue("trimAndSet - String value should empty string", "".equals(builder.getDbUser()));

        // test string with trailing whitespace
        ra.setDbUser(" aa ");
        builder = Datasource.Accessor.newBuilder();
        JsonRecordTransformer.trimAndSet(builder::setDbUser, ra::getDbUser);
        Assert.assertTrue("trimAndSet - String value should 'aa'", "aa".equals(builder.getDbUser()));

        // test string without trailing whitespace
        ra.setDbUser("aaa");
        builder = Datasource.Accessor.newBuilder();
        JsonRecordTransformer.trimAndSet(builder::setDbUser, ra::getDbUser);
        Assert.assertTrue("trimAndSet - String value should 'aaa'", "aaa".equals(builder.getDbUser()));

        //-------trimAndSetIfNotEmpty----------//
        ra.setDbUser(null);
        builder = Datasource.Accessor.newBuilder();
        try {
            JsonRecordTransformer.trimAndSetIfNotEmpty(builder::setDbUser, ra::getDbUser);
        } catch (Exception e){
            Assert.assertTrue("trimAndSetIfNotEmpty - Invalid exception is thrown for null in property value", e.getClass().equals(NullPointerException.class));
        }

        // test empty string
        ra.setDbUser("  ");
        builder = Datasource.Accessor.newBuilder();
        String valueBefore = builder.getDbUser();
        JsonRecordTransformer.trimAndSetIfNotEmpty(builder::setDbUser, ra::getDbUser);
        String valueAfter = builder.getDbUser();
        Assert.assertTrue("trimAndSet - String value should not be set", valueBefore.equals(valueAfter));

        // test string with trailing whitespace
        ra.setDbUser(" aa ");
        builder = Datasource.Accessor.newBuilder();
        JsonRecordTransformer.trimAndSetIfNotEmpty(builder::setDbUser, ra::getDbUser);
        Assert.assertTrue("trimAndSet - String value should 'aa'", "aa".equals(builder.getDbUser()));

        // test string without trailing whitespace
        ra.setDbUser("aaa");
        builder = Datasource.Accessor.newBuilder();
        JsonRecordTransformer.trimAndSetIfNotEmpty(builder::setDbUser, ra::getDbUser);
        Assert.assertTrue("trimAndSet - String value should 'aaa'", "aaa".equals(builder.getDbUser()));

    }

    @Test
    public void testSessionId(){

        Record record = (new Gson()).fromJson(recordString, Record.class);
        JsonRecordTransformer transformer = new JsonRecordTransformer();

        Long hashOfOne = -4266524885998029950L;
        record.setSessionId("1");
        Long sessionId = transformer.getSessionIdForSniffer(record);
        Assert.assertTrue("invalid sessionId for given sessionId property", hashOfOne.equals(sessionId)/*correct hash for 1*/);

        Long hashOfRecordString = -1721301020957000723L;
        record.setSessionId("");
        sessionId = transformer.getSessionIdForSniffer(record);
        Assert.assertTrue("invalid sessionId for empty sessionId property", hashOfRecordString.equals(sessionId));

        record.setSessionId(" ");
        sessionId = transformer.getSessionIdForSniffer(record);
        Assert.assertTrue("invalid sessionId for empty with space sessionId property", hashOfRecordString.equals(sessionId));

        record.setSessionId(null);
        sessionId = transformer.getSessionIdForSniffer(record);
        Assert.assertTrue("invalid sessionId for null sessionId property", hashOfRecordString.equals(sessionId));
    }


    @Test
    public void testIp() {

        Record record = (new Gson()).fromJson(recordString, Record.class);
        JsonRecordTransformer transformer = new JsonRecordTransformer();
        Datasource.Session_locator session_locator = transformer.buildSessionLocator(record);

        Assert.assertTrue("failed to set ip type", !session_locator.getIsIpv6());
        Assert.assertTrue("failed to set client ip", 18088063==session_locator.getClientIp());
        Assert.assertTrue("failed to set server ip", 18743423==session_locator.getServerIp());
    }

    @Test
    public void testIpV6() {

        Record record = (new Gson()).fromJson(msgTemplateIPV6, Record.class);
        JsonRecordTransformer transformer = new JsonRecordTransformer();
        Datasource.Session_locator session_locator = transformer.buildSessionLocator(record);

        Assert.assertTrue("failed to set ipv6 type", session_locator.getIsIpv6());
        Assert.assertTrue("failed to set client ip", "1:1:1:1:1:1:1:1".equals(session_locator.getClientIpv6()));
        Assert.assertTrue("failed to set server ip", "2001:0db8:0000:0000:0000:ff00:0042:8329".equals(session_locator.getServerIpv6()));
    }

    @Test
    public void testMixedIpClientV6() {

        Record record = (new Gson()).fromJson(msgTemplateMixedIpClientV6, Record.class);
        JsonRecordTransformer transformer = new JsonRecordTransformer();
        Datasource.Session_locator session_locator = transformer.buildSessionLocator(record);

        Assert.assertTrue("failed to set ipv6 type", session_locator.getIsIpv6());
        Assert.assertTrue("failed to set client ip", "1:1:1:1:1:1:1:1".equals(session_locator.getClientIpv6()));
        Assert.assertTrue("failed to set server ip", 67305985==session_locator.getServerIp());
    }

    @Test
    public void testMixedIpServerV6() {

        Record record = (new Gson()).fromJson(msgTemplateMixedIPServerV6, Record.class);
        JsonRecordTransformer transformer = new JsonRecordTransformer();
        Datasource.Session_locator session_locator = transformer.buildSessionLocator(record);

        Assert.assertTrue("failed to set ipv6 type", session_locator.getIsIpv6());
        Assert.assertTrue("failed to set client ip", 67305985==session_locator.getClientIp());
        Assert.assertTrue("failed to set server ip", "2001:0db8:0000:0000:0000:ff00:0042:8329".equals(session_locator.getServerIpv6()));
    }


    @Test
    public void testIpTranslation() {

        JsonRecordTransformer transformer = new JsonRecordTransformer();
        int val = transformer.convert_ipstr_to_int("9.70.147.59");
        Assert.assertEquals("Failed to parse 9.70.147.59",val,999507465);

        val = transformer.convert_ipstr_to_int("127.0.0.1");
        Assert.assertEquals("Failed to parse 127.0.0.1",val,16777343);

        val = transformer.convert_ipstr_to_int("0.0.0.0");
        Assert.assertEquals("Failed to parse 0.0.0.0",val,0);

        val = transformer.convert_ipstr_to_int("::ffff:123.45.67.89");
        System.out.println("val "+val);
    }

    public static final String msgTemplateIPV6="{\n" +
            "\t\"sessionId\": \"\",\n" +
            "\t\"dbName\": \"admin\",\n" +
            "\t\"appUserName\": \"\",\n" +
            "\t\"time\":{ \"timstamp\": 1581841318, \"minOffsetFromGMT\":2, \"minDst\":0 },\n" +
            "\t\"sessionLocator\": {\n" +
            "\t\t\"clientIp\": \"\",\n" +
            "\t\t\"clientPort\": 36802,\n" +
            "\t\t\"serverIp\": \"\",\n" +
            "\t\t\"serverPort\": 111,\n" +
            "\t\t\"isIpv6\": true,\n" +
            "\t\t\"clientIpv6\": \"1:1:1:1:1:1:1:1\",\n" +
            "\t\t\"serverIpv6\": \"2001:0db8:0000:0000:0000:ff00:0042:8329\"\n" +
            "\t},\n" +
            "\t\"accessor\": {\n" +
            "\t\t\"dbUser\": \"QQQ\",\n" +
            "\t\t\"serverType\": \"MongoDB\",\n" +
            "\t\t\"serverOs\": \"\",\n" +
            "\t\t\"clientOs\": \"\",\n" +
            "\t\t\"clientHostName\": \"\",\n" +
            "\t\t\"serverHostName\": \"AAAAAA\",\n" +
            "\t\t\"commProtocol\": \"\",\n" +
            "\t\t\"dbProtocol\": \"MongoDB native audit\",\n" +
            "\t\t\"dbProtocolVersion\": \"\",\n" +
            "\t\t\"osUser\": \"\",\n" +
            "\t\t\"sourceProgram\": \"mongod\",\n" +
            "\t\t\"client_mac\": \"\",\n" +
            "\t\t\"serverDescription\": \"\",\n" +
            "\t\t\"serviceName\": \"admin\",\n" +
            "\t\t\"language\": \"FREE_TEXT\",\n" +
            "\t\t\"dataType\": \"CONSTRUCT\"\n" +
            "\t},\n" +
            "\t\"data\": {\n" +
            "\t\t\"construct\": {\n" +
            "\t\t\t\"sentences\": [\n" +
            "\t\t\t\t{\n" +
            "\t\t\t\t\t\"verb\": \"insert\",\n" +
            "\t\t\t\t\t\"objects\": [\n" +
            "\t\t\t\t\t\t{\n" +
            "\t\t\t\t\t\t\t\"name\": \"system.users.BBBB\",\n" +
            "\t\t\t\t\t\t\t\"type\": \"collection\",\n" +
            "\t\t\t\t\t\t\t\"fields\": [],\n" +
            "\t\t\t\t\t\t\t\"schema\": \"\"\n" +
            "\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t],\n" +
            "\t\t\t\t\t\"descendants\": [],\n" +
            "\t\t\t\t\t\"fields\": []\n" +
            "\t\t\t\t}\n" +
            "\t\t\t],\n" +
            "\t\t\t\"fullSql\": \"{\\\"atype\\\":\\\"authCheck\\\",\\\"ts\\\":{\\\"$date\\\":\\\"2020-08-13T05:56:26.518-0400\\\"},\\\"local\\\":{\\\"ip\\\":\\\"127.0.0.1\\\",\\\"port\\\":27017},\\\"remote\\\":{\\\"ip\\\":\\\"127.0.0.1\\\",\\\"port\\\":36802},\\\"users\\\":[{\\\"user\\\":\\\"admin\\\",\\\"db\\\":\\\"admin\\\"}],\\\"roles\\\":[{\\\"role\\\":\\\"root\\\",\\\"db\\\":\\\"admin\\\"}],\\\"param\\\":{\\\"command\\\":\\\"insert\\\",\\\"ns\\\":\\\"admin.system.users\\\",\\\"args\\\":{\\\"insert\\\":\\\"system.users\\\",\\\"bypassDocumentValidation\\\":false,\\\"ordered\\\":true,\\\"$db\\\":\\\"admin\\\",\\\"documents\\\":[{\\\"_id\\\":\\\"admin.accountAdmin01\\\",\\\"userId\\\":{\\\"$binary\\\":\\\"afqdFgM0QRWMrjkX7iYbGg==\\\",\\\"$type\\\":\\\"04\\\"},\\\"user\\\":\\\"accountAdmin01\\\",\\\"db\\\":\\\"admin\\\",\\\"credentials\\\":{\\\"SCRAM-SHA-1\\\":{\\\"iterationCount\\\":10000,\\\"salt\\\":\\\"Z29DxpJONJhacqELtKMaTg==\\\",\\\"storedKey\\\":\\\"qB0+KFWmWp+ri6Q+S8nmuPrpnUI=\\\",\\\"serverKey\\\":\\\"hy/3s2oajbPM7WEtNeQqWriUUNg=\\\"},\\\"SCRAM-SHA-256\\\":{\\\"iterationCount\\\":15000,\\\"salt\\\":\\\"6QiTwW12fv+la9VhbzTl3Uv7RdBNrox1SdirtQ==\\\",\\\"storedKey\\\":\\\"IIKOj7fIs5YgipTW/KH1dybI80OcgYwyg6WixwSyAys=\\\",\\\"serverKey\\\":\\\"USBpc2bFYigXyTx5CG1tHXJWvJJ3NlQrbJY7arUKKr4=\\\"}},\\\"customData\\\":{\\\"employeeId\\\":12345},\\\"roles\\\":[{\\\"role\\\":\\\"clusterAdmin\\\",\\\"db\\\":\\\"admin\\\"},{\\\"role\\\":\\\"readAnyDatabase\\\",\\\"db\\\":\\\"admin\\\"},{\\\"role\\\":\\\"readWrite\\\",\\\"db\\\":\\\"admin\\\"}]}]}},\\\"result\\\":0}\",\n" +
            "\t\t\t\"redactedSensitiveDataSql\": \"{\\\"atype\\\":\\\"authCheck\\\",\\\"ts\\\":{\\\"$date\\\":\\\"2020-08-13T05:56:26.518-0400\\\"},\\\"local\\\":{\\\"ip\\\":\\\"127.0.0.1\\\",\\\"port\\\":27017},\\\"remote\\\":{\\\"ip\\\":\\\"127.0.0.1\\\",\\\"port\\\":36802},\\\"users\\\":[{\\\"user\\\":\\\"admin\\\",\\\"db\\\":\\\"admin\\\"}],\\\"roles\\\":[{\\\"role\\\":\\\"root\\\",\\\"db\\\":\\\"admin\\\"}],\\\"param\\\":{\\\"command\\\":\\\"insert\\\",\\\"ns\\\":\\\"admin.system.users\\\",\\\"args\\\":{\\\"ordered\\\":\\\"?\\\",\\\"documents\\\":[{\\\"credentials\\\":{\\\"SCRAM-SHA-1\\\":{\\\"iterationCount\\\":\\\"?\\\",\\\"salt\\\":\\\"?\\\",\\\"serverKey\\\":\\\"?\\\",\\\"storedKey\\\":\\\"?\\\"},\\\"SCRAM-SHA-256\\\":{\\\"iterationCount\\\":\\\"?\\\",\\\"salt\\\":\\\"?\\\",\\\"serverKey\\\":\\\"?\\\",\\\"storedKey\\\":\\\"?\\\"}},\\\"roles\\\":[{\\\"role\\\":\\\"?\\\",\\\"db\\\":\\\"?\\\"},{\\\"role\\\":\\\"?\\\",\\\"db\\\":\\\"?\\\"},{\\\"role\\\":\\\"?\\\",\\\"db\\\":\\\"?\\\"}],\\\"customData\\\":{\\\"employeeId\\\":\\\"?\\\"},\\\"_id\\\":\\\"?\\\",\\\"userId\\\":{\\\"$binary\\\":\\\"?\\\",\\\"$type\\\":\\\"?\\\"},\\\"user\\\":\\\"?\\\",\\\"db\\\":\\\"?\\\"}],\\\"bypassDocumentValidation\\\":\\\"?\\\",\\\"insert\\\":\\\"system.users\\\",\\\"$db\\\":\\\"admin\\\"}},\\\"result\\\":0}\"\n" +
            "\t\t},\n" +
            "\t\t\"originalSqlCommand\": \"\",\n" +
            "\t\t\"useConstruct\": true\n" +
            "\t},\n" +
            "\t\"exception\": null\n" +
            "}";

    public static final String msgTemplateMixedIPServerV6="{\n" +
            "\t\"sessionId\": \"\",\n" +
            "\t\"dbName\": \"admin\",\n" +
            "\t\"appUserName\": \"\",\n" +
            "\t\"time\":{ \"timstamp\": 1581841318, \"minOffsetFromGMT\":2, \"minDst\":0 },\n" +
            "\t\"sessionLocator\": {\n" +
            "\t\t\"clientIp\": \"\",\n" +
            "\t\t\"clientPort\": 36802,\n" +
            "\t\t\"serverIp\": \"\",\n" +
            "\t\t\"serverPort\": 111,\n" +
            "\t\t\"isIpv6\": true,\n" +
            "\t\t\"clientIp\": \"1.2.3.4\",\n" +
            "\t\t\"serverIpv6\": \"2001:0db8:0000:0000:0000:ff00:0042:8329\"\n" +
            "\t},\n" +
            "\t\"accessor\": {\n" +
            "\t\t\"dbUser\": \"QQQ\",\n" +
            "\t\t\"serverType\": \"MongoDB\",\n" +
            "\t\t\"serverOs\": \"\",\n" +
            "\t\t\"clientOs\": \"\",\n" +
            "\t\t\"clientHostName\": \"\",\n" +
            "\t\t\"serverHostName\": \"AAAAAA\",\n" +
            "\t\t\"commProtocol\": \"\",\n" +
            "\t\t\"dbProtocol\": \"MongoDB native audit\",\n" +
            "\t\t\"dbProtocolVersion\": \"\",\n" +
            "\t\t\"osUser\": \"\",\n" +
            "\t\t\"sourceProgram\": \"mongod\",\n" +
            "\t\t\"client_mac\": \"\",\n" +
            "\t\t\"serverDescription\": \"\",\n" +
            "\t\t\"serviceName\": \"admin\",\n" +
            "\t\t\"language\": \"FREE_TEXT\",\n" +
            "\t\t\"dataType\": \"CONSTRUCT\"\n" +
            "\t},\n" +
            "\t\"data\": {\n" +
            "\t\t\"construct\": {\n" +
            "\t\t\t\"sentences\": [\n" +
            "\t\t\t\t{\n" +
            "\t\t\t\t\t\"verb\": \"insert\",\n" +
            "\t\t\t\t\t\"objects\": [\n" +
            "\t\t\t\t\t\t{\n" +
            "\t\t\t\t\t\t\t\"name\": \"system.users.BBBB\",\n" +
            "\t\t\t\t\t\t\t\"type\": \"collection\",\n" +
            "\t\t\t\t\t\t\t\"fields\": [],\n" +
            "\t\t\t\t\t\t\t\"schema\": \"\"\n" +
            "\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t],\n" +
            "\t\t\t\t\t\"descendants\": [],\n" +
            "\t\t\t\t\t\"fields\": []\n" +
            "\t\t\t\t}\n" +
            "\t\t\t],\n" +
            "\t\t\t\"fullSql\": \"{\\\"atype\\\":\\\"authCheck\\\",\\\"ts\\\":{\\\"$date\\\":\\\"2020-08-13T05:56:26.518-0400\\\"},\\\"local\\\":{\\\"ip\\\":\\\"127.0.0.1\\\",\\\"port\\\":27017},\\\"remote\\\":{\\\"ip\\\":\\\"127.0.0.1\\\",\\\"port\\\":36802},\\\"users\\\":[{\\\"user\\\":\\\"admin\\\",\\\"db\\\":\\\"admin\\\"}],\\\"roles\\\":[{\\\"role\\\":\\\"root\\\",\\\"db\\\":\\\"admin\\\"}],\\\"param\\\":{\\\"command\\\":\\\"insert\\\",\\\"ns\\\":\\\"admin.system.users\\\",\\\"args\\\":{\\\"insert\\\":\\\"system.users\\\",\\\"bypassDocumentValidation\\\":false,\\\"ordered\\\":true,\\\"$db\\\":\\\"admin\\\",\\\"documents\\\":[{\\\"_id\\\":\\\"admin.accountAdmin01\\\",\\\"userId\\\":{\\\"$binary\\\":\\\"afqdFgM0QRWMrjkX7iYbGg==\\\",\\\"$type\\\":\\\"04\\\"},\\\"user\\\":\\\"accountAdmin01\\\",\\\"db\\\":\\\"admin\\\",\\\"credentials\\\":{\\\"SCRAM-SHA-1\\\":{\\\"iterationCount\\\":10000,\\\"salt\\\":\\\"Z29DxpJONJhacqELtKMaTg==\\\",\\\"storedKey\\\":\\\"qB0+KFWmWp+ri6Q+S8nmuPrpnUI=\\\",\\\"serverKey\\\":\\\"hy/3s2oajbPM7WEtNeQqWriUUNg=\\\"},\\\"SCRAM-SHA-256\\\":{\\\"iterationCount\\\":15000,\\\"salt\\\":\\\"6QiTwW12fv+la9VhbzTl3Uv7RdBNrox1SdirtQ==\\\",\\\"storedKey\\\":\\\"IIKOj7fIs5YgipTW/KH1dybI80OcgYwyg6WixwSyAys=\\\",\\\"serverKey\\\":\\\"USBpc2bFYigXyTx5CG1tHXJWvJJ3NlQrbJY7arUKKr4=\\\"}},\\\"customData\\\":{\\\"employeeId\\\":12345},\\\"roles\\\":[{\\\"role\\\":\\\"clusterAdmin\\\",\\\"db\\\":\\\"admin\\\"},{\\\"role\\\":\\\"readAnyDatabase\\\",\\\"db\\\":\\\"admin\\\"},{\\\"role\\\":\\\"readWrite\\\",\\\"db\\\":\\\"admin\\\"}]}]}},\\\"result\\\":0}\",\n" +
            "\t\t\t\"redactedSensitiveDataSql\": \"{\\\"atype\\\":\\\"authCheck\\\",\\\"ts\\\":{\\\"$date\\\":\\\"2020-08-13T05:56:26.518-0400\\\"},\\\"local\\\":{\\\"ip\\\":\\\"127.0.0.1\\\",\\\"port\\\":27017},\\\"remote\\\":{\\\"ip\\\":\\\"127.0.0.1\\\",\\\"port\\\":36802},\\\"users\\\":[{\\\"user\\\":\\\"admin\\\",\\\"db\\\":\\\"admin\\\"}],\\\"roles\\\":[{\\\"role\\\":\\\"root\\\",\\\"db\\\":\\\"admin\\\"}],\\\"param\\\":{\\\"command\\\":\\\"insert\\\",\\\"ns\\\":\\\"admin.system.users\\\",\\\"args\\\":{\\\"ordered\\\":\\\"?\\\",\\\"documents\\\":[{\\\"credentials\\\":{\\\"SCRAM-SHA-1\\\":{\\\"iterationCount\\\":\\\"?\\\",\\\"salt\\\":\\\"?\\\",\\\"serverKey\\\":\\\"?\\\",\\\"storedKey\\\":\\\"?\\\"},\\\"SCRAM-SHA-256\\\":{\\\"iterationCount\\\":\\\"?\\\",\\\"salt\\\":\\\"?\\\",\\\"serverKey\\\":\\\"?\\\",\\\"storedKey\\\":\\\"?\\\"}},\\\"roles\\\":[{\\\"role\\\":\\\"?\\\",\\\"db\\\":\\\"?\\\"},{\\\"role\\\":\\\"?\\\",\\\"db\\\":\\\"?\\\"},{\\\"role\\\":\\\"?\\\",\\\"db\\\":\\\"?\\\"}],\\\"customData\\\":{\\\"employeeId\\\":\\\"?\\\"},\\\"_id\\\":\\\"?\\\",\\\"userId\\\":{\\\"$binary\\\":\\\"?\\\",\\\"$type\\\":\\\"?\\\"},\\\"user\\\":\\\"?\\\",\\\"db\\\":\\\"?\\\"}],\\\"bypassDocumentValidation\\\":\\\"?\\\",\\\"insert\\\":\\\"system.users\\\",\\\"$db\\\":\\\"admin\\\"}},\\\"result\\\":0}\"\n" +
            "\t\t},\n" +
            "\t\t\"originalSqlCommand\": \"\",\n" +
            "\t\t\"useConstruct\": true\n" +
            "\t},\n" +
            "\t\"exception\": null\n" +
            "}";

    public static final String msgTemplateMixedIpClientV6 ="{\n" +
            "\t\"sessionId\": \"\",\n" +
            "\t\"dbName\": \"admin\",\n" +
            "\t\"appUserName\": \"\",\n" +
            "\t\"time\":{ \"timstamp\": 1581841318, \"minOffsetFromGMT\":2, \"minDst\":0 },\n" +
            "\t\"sessionLocator\": {\n" +
            "\t\t\"clientIp\": \"\",\n" +
            "\t\t\"clientPort\": 36802,\n" +
            "\t\t\"serverIp\": \"\",\n" +
            "\t\t\"serverPort\": 111,\n" +
            "\t\t\"isIpv6\": true,\n" +
            "\t\t\"clientIpv6\": \"1:1:1:1:1:1:1:1\",\n" +
            "\t\t\"serverIp\": \"1.2.3.4\"\n" +
            "\t},\n" +
            "\t\"accessor\": {\n" +
            "\t\t\"dbUser\": \"QQQ\",\n" +
            "\t\t\"serverType\": \"MongoDB\",\n" +
            "\t\t\"serverOs\": \"\",\n" +
            "\t\t\"clientOs\": \"\",\n" +
            "\t\t\"clientHostName\": \"\",\n" +
            "\t\t\"serverHostName\": \"AAAAAA\",\n" +
            "\t\t\"commProtocol\": \"\",\n" +
            "\t\t\"dbProtocol\": \"MongoDB native audit\",\n" +
            "\t\t\"dbProtocolVersion\": \"\",\n" +
            "\t\t\"osUser\": \"\",\n" +
            "\t\t\"sourceProgram\": \"mongod\",\n" +
            "\t\t\"client_mac\": \"\",\n" +
            "\t\t\"serverDescription\": \"\",\n" +
            "\t\t\"serviceName\": \"admin\",\n" +
            "\t\t\"language\": \"FREE_TEXT\",\n" +
            "\t\t\"dataType\": \"CONSTRUCT\"\n" +
            "\t},\n" +
            "\t\"data\": {\n" +
            "\t\t\"construct\": {\n" +
            "\t\t\t\"sentences\": [\n" +
            "\t\t\t\t{\n" +
            "\t\t\t\t\t\"verb\": \"insert\",\n" +
            "\t\t\t\t\t\"objects\": [\n" +
            "\t\t\t\t\t\t{\n" +
            "\t\t\t\t\t\t\t\"name\": \"system.users.BBBB\",\n" +
            "\t\t\t\t\t\t\t\"type\": \"collection\",\n" +
            "\t\t\t\t\t\t\t\"fields\": [],\n" +
            "\t\t\t\t\t\t\t\"schema\": \"\"\n" +
            "\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t],\n" +
            "\t\t\t\t\t\"descendants\": [],\n" +
            "\t\t\t\t\t\"fields\": []\n" +
            "\t\t\t\t}\n" +
            "\t\t\t],\n" +
            "\t\t\t\"fullSql\": \"{\\\"atype\\\":\\\"authCheck\\\",\\\"ts\\\":{\\\"$date\\\":\\\"2020-08-13T05:56:26.518-0400\\\"},\\\"local\\\":{\\\"ip\\\":\\\"127.0.0.1\\\",\\\"port\\\":27017},\\\"remote\\\":{\\\"ip\\\":\\\"127.0.0.1\\\",\\\"port\\\":36802},\\\"users\\\":[{\\\"user\\\":\\\"admin\\\",\\\"db\\\":\\\"admin\\\"}],\\\"roles\\\":[{\\\"role\\\":\\\"root\\\",\\\"db\\\":\\\"admin\\\"}],\\\"param\\\":{\\\"command\\\":\\\"insert\\\",\\\"ns\\\":\\\"admin.system.users\\\",\\\"args\\\":{\\\"insert\\\":\\\"system.users\\\",\\\"bypassDocumentValidation\\\":false,\\\"ordered\\\":true,\\\"$db\\\":\\\"admin\\\",\\\"documents\\\":[{\\\"_id\\\":\\\"admin.accountAdmin01\\\",\\\"userId\\\":{\\\"$binary\\\":\\\"afqdFgM0QRWMrjkX7iYbGg==\\\",\\\"$type\\\":\\\"04\\\"},\\\"user\\\":\\\"accountAdmin01\\\",\\\"db\\\":\\\"admin\\\",\\\"credentials\\\":{\\\"SCRAM-SHA-1\\\":{\\\"iterationCount\\\":10000,\\\"salt\\\":\\\"Z29DxpJONJhacqELtKMaTg==\\\",\\\"storedKey\\\":\\\"qB0+KFWmWp+ri6Q+S8nmuPrpnUI=\\\",\\\"serverKey\\\":\\\"hy/3s2oajbPM7WEtNeQqWriUUNg=\\\"},\\\"SCRAM-SHA-256\\\":{\\\"iterationCount\\\":15000,\\\"salt\\\":\\\"6QiTwW12fv+la9VhbzTl3Uv7RdBNrox1SdirtQ==\\\",\\\"storedKey\\\":\\\"IIKOj7fIs5YgipTW/KH1dybI80OcgYwyg6WixwSyAys=\\\",\\\"serverKey\\\":\\\"USBpc2bFYigXyTx5CG1tHXJWvJJ3NlQrbJY7arUKKr4=\\\"}},\\\"customData\\\":{\\\"employeeId\\\":12345},\\\"roles\\\":[{\\\"role\\\":\\\"clusterAdmin\\\",\\\"db\\\":\\\"admin\\\"},{\\\"role\\\":\\\"readAnyDatabase\\\",\\\"db\\\":\\\"admin\\\"},{\\\"role\\\":\\\"readWrite\\\",\\\"db\\\":\\\"admin\\\"}]}]}},\\\"result\\\":0}\",\n" +
            "\t\t\t\"redactedSensitiveDataSql\": \"{\\\"atype\\\":\\\"authCheck\\\",\\\"ts\\\":{\\\"$date\\\":\\\"2020-08-13T05:56:26.518-0400\\\"},\\\"local\\\":{\\\"ip\\\":\\\"127.0.0.1\\\",\\\"port\\\":27017},\\\"remote\\\":{\\\"ip\\\":\\\"127.0.0.1\\\",\\\"port\\\":36802},\\\"users\\\":[{\\\"user\\\":\\\"admin\\\",\\\"db\\\":\\\"admin\\\"}],\\\"roles\\\":[{\\\"role\\\":\\\"root\\\",\\\"db\\\":\\\"admin\\\"}],\\\"param\\\":{\\\"command\\\":\\\"insert\\\",\\\"ns\\\":\\\"admin.system.users\\\",\\\"args\\\":{\\\"ordered\\\":\\\"?\\\",\\\"documents\\\":[{\\\"credentials\\\":{\\\"SCRAM-SHA-1\\\":{\\\"iterationCount\\\":\\\"?\\\",\\\"salt\\\":\\\"?\\\",\\\"serverKey\\\":\\\"?\\\",\\\"storedKey\\\":\\\"?\\\"},\\\"SCRAM-SHA-256\\\":{\\\"iterationCount\\\":\\\"?\\\",\\\"salt\\\":\\\"?\\\",\\\"serverKey\\\":\\\"?\\\",\\\"storedKey\\\":\\\"?\\\"}},\\\"roles\\\":[{\\\"role\\\":\\\"?\\\",\\\"db\\\":\\\"?\\\"},{\\\"role\\\":\\\"?\\\",\\\"db\\\":\\\"?\\\"},{\\\"role\\\":\\\"?\\\",\\\"db\\\":\\\"?\\\"}],\\\"customData\\\":{\\\"employeeId\\\":\\\"?\\\"},\\\"_id\\\":\\\"?\\\",\\\"userId\\\":{\\\"$binary\\\":\\\"?\\\",\\\"$type\\\":\\\"?\\\"},\\\"user\\\":\\\"?\\\",\\\"db\\\":\\\"?\\\"}],\\\"bypassDocumentValidation\\\":\\\"?\\\",\\\"insert\\\":\\\"system.users\\\",\\\"$db\\\":\\\"admin\\\"}},\\\"result\\\":0}\"\n" +
            "\t\t},\n" +
            "\t\t\"originalSqlCommand\": \"\",\n" +
            "\t\t\"useConstruct\": true\n" +
            "\t},\n" +
            "\t\"exception\": null\n" +
            "}";

}
