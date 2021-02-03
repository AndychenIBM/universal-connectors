package org.logstashplugins;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Event;
import com.ibm.guardium.universalconnector.common.GuardConstants;
import com.ibm.guardium.universalconnector.transformer.JsonRecordTransformer;
import org.apache.logging.log4j.core.util.Assert;
import org.junit.Test;
import org.logstash.plugins.ConfigurationImpl;
import sun.net.util.IPAddressUtil;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.*;

public class JavaOutputExampleTest {

    //@Test
    public void testActualSendingDataToRealMachine_POSTRGRE() throws InterruptedException {
        //GRD-43047
        String prefix = "Prefix";
        Map<String, Object> configValues = new HashMap<>();
        configValues.put(JavaOutputToGuardium.PREFIX_CONFIG.name(), prefix);
        Configuration config = new ConfigurationImpl(configValues);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JavaOutputToGuardium output = new JavaOutputToGuardium("test-id", config, null, baos);

        Collection<Event> events = new ArrayList<>();

        Event e = new org.logstash.Event();
        boolean keepGoing = true;
        Random random = new Random();

        int count = 2;
        while (keepGoing && count-->0) {
            Date time = new Date();
            int ipSuffix = random.ints(0, 1).findFirst().getAsInt();
            int serverPort = random.ints(0, 3).findFirst().getAsInt();
            System.out.println("serverPort " + serverPort);
            System.out.println("ipSuffix " + ipSuffix);

            String record = String.format(POSTGRE_record, time.getTime(), ipSuffix, serverPort);
            e.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, record);
            events.add(e);
            output.output(events);

//            e.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, s3Record);
//            events.add(e);
//            output.output(events);

            Thread.sleep(10000);
        }
    }


    //@Test
    public void testActualSendingDataToRealMachine() throws InterruptedException {
        //GRD-43047
        String prefix = "Prefix";
        Map<String, Object> configValues = new HashMap<>();
        configValues.put(JavaOutputToGuardium.PREFIX_CONFIG.name(), prefix);
        Configuration config = new ConfigurationImpl(configValues);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JavaOutputToGuardium output = new JavaOutputToGuardium("test-id", config, null, baos);

        Collection<Event> events = new ArrayList<>();

        Event e = new org.logstash.Event();
        boolean keepGoing = true;
        Random random = new Random();

        int count = 2;
        while (keepGoing && count-->0) {
            Date time = new Date();
            int ipSuffix = random.ints(0, 1).findFirst().getAsInt();
            int serverPort = random.ints(0, 3).findFirst().getAsInt();
            System.out.println("serverPort " + serverPort);
            System.out.println("ipSuffix " + ipSuffix);

            String record = String.format(msgTemplateIPV6, time.getTime(), ipSuffix, serverPort);
            //String record = String.format(POSTGRE_record, time.getTime(), ipSuffix, serverPort);
            //e.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, s3Record);

            e.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, record);
//            e.setField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME, String.format(mysqlMsg, time.getTime()));

            events.add(e);

            output.output(events);

            Thread.sleep(10000);
        }
    }

    //@Test
    public void testJavaOutputExceptionExample() {

        String prefix = "Prefix";
        Map<String, Object> configValues = new HashMap<>();
        configValues.put(JavaOutputToGuardium.PREFIX_CONFIG.name(), prefix);
        Configuration config = new ConfigurationImpl(configValues);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JavaOutputToGuardium output = new JavaOutputToGuardium("test-id", config, null, baos);

        String sourceField = "message";
        int eventCount = 5;
        Collection<Event> events = new ArrayList<>();
        /*for (int k = 0; k < eventCount; k++) {
            Event e = new org.logstash.Event();
            e.setField(sourceField, "message " + k);
            events.add(e);
        }*/
        //add mongoDB syslog event
        Event e = new org.logstash.Event();
        e.setField("timestamp", "2020-05-04T05:09:06.149-0500");
        e.setField("Record", "{\"sessionId\":\"0vpeEPORRFecjnJihLQbqQ\\u003d\\u003d\",\"dbName\":\"admin\",\"appUserName\":\"\",\"time\":1595237402291,\"sessionLocator\":{\"clientIp\":\"9.42.29.56\",\"clientPort\":41050,\"serverIp\":\"9.42.29.56\",\"serverPort\":27017,\"isIpv6\":false,\"clientIpv6\":\"\",\"serverIpv6\":\"\"},\"accessor\":{\"dbUser\":\"Kerri \",\"serverType\":\"MongoDB\",\"serverOs\":\"\",\"clientOs\":\"\",\"clientHostName\":\"\",\"serverHostName\":\"dbqa111\",\"commProtocol\":\"\",\"dbProtocol\":\"MongoDB native audit\",\"dbProtocolVersion\":\"\",\"osUser\":\"\",\"sourceProgram\":\"mongod\",\"client_mac\":\"\",\"serverDescription\":\"\",\"serviceName\":\"admin\",\"language\":\"FREE_TEXT\",\"type\":\"CONSTRUCT\"},\"data\":{\"construct\":{\"sentences\":[{\"verb\":\"create\",\"objects\":[{\"name\":\"customer_A126\",\"type\":\"collection\",\"fields\":[],\"schema\":\"\"}],\"descendants\":[],\"fields\":[]}],\"full_sql\":\"{\"atype\":\"authCheck\",\"ts\":{\"$date\":\"2020-07-20T05:30:02.291-0400\"},\"local\":{\"ip\":\"9.42.29.56\",\"port\":27017},\"remote\":{\"ip\":\"9.42.29.56\",\"port\":41050},\"users\":[{\"user\":\"Kerri\",\"db\":\"admin\"}],\"roles\":[{\"role\":\"root\",\"db\":\"admin\"}],\"param\":{\"command\":\"create\",\"ns\":\"admin.customer_A126\",\"args\":{\"create\":\"customer_A126\",\"lsid\":{\"id\":{\"$binary\":\"0vpeEPORRFecjnJihLQbqQ\\u003d\\u003d\",\"$type\":\"04\"}},\"$db\":\"admin\",\"$readPreference\":{\"mode\":\"primary\"}}},\"result\":0}\",\"original_sql\":\"{\"atype\":\"authCheck\",\"ts\":{\"$date\":\"2020-07-20T05:30:02.291-0400\"},\"local\":{\"ip\":\"9.42.29.56\",\"port\":27017},\"remote\":{\"ip\":\"9.42.29.56\",\"port\":41050},\"users\":[{\"user\":\"Kerri\",\"db\":\"admin\"}],\"roles\":[{\"role\":\"root\",\"db\":\"admin\"}],\"param\":{\"command\":\"create\",\"ns\":\"admin.customer_A126\",\"args\":{\"lsid\":{\"id\":{\"$binary\":\"?\",\"$type\":\"?\"}},\"$readPreference\":{\"mode\":\"?\"},\"create\":\"customer_A126\",\"$db\":\"admin\"}},\"result\":0}\"},\"timestamp\":1595237402291,\"originalSqlCommand\":\"\",\"useConstruct\":true},\"exception\":null}");
        e.setField("server_type", "MONGODB");
        e.setField("server_hostname", "qa-db51");

        events.add(e);


        output.output(events);

    }


    public static final String msgTemplateIPV6 = "{\n" +
            "\t\"sessionId\": \"\",\n" +
            "\t\"dbName\": \"admin\",\n" +
            "\t\"appUserName\": \"\",\n" +
            "\t\"time\": {\"timestamp\":%d, \"minOffsetFromGMT\":0, \"minDst\":0},\n" +
            "\t\"sessionLocator\": {\n" +
            "\t\t\"clientIp\": \"\",\n" +
            "\t\t\"clientPort\": 36802,\n" +
            "\t\t\"serverIp\": \"\",\n" +
            "\t\t\"serverPort\": %d,\n" +
            "\t\t\"isIpv6\": true,\n" +
            "\t\t\"clientIpv6\": \"1:1:1:1:1:1:1:%d\",\n" +
            "\t\t\"serverIpv6\": \"2001:0db8:0000:0000:0000:ff00:0042:8329\"\n" +
            "\t},\n" +
            "\t\"accessor\": {\n" +
            "\t\t\"dbUser\": \"abcdUserNameWithSpaceAtEnd \",\n" +
            "\t\t\"serverType\": \"MongoDB\",\n" +
            "\t\t\"serverOs\": \"\",\n" +
            "\t\t\"clientOs\": \"\",\n" +
            "\t\t\"clientHostName\": \"\",\n" +
            "\t\t\"serverHostName\": \"AAAAAAAAAA\",\n" +
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

    public static String s3Record = "{\"sessionId\":\"{\\\"invokedBy\\\":\\\"cloudtrail.amazonaws.com\\\",\\\"type\\\":\\\"AWSService\\\"}\",\"dbName\":\"ucdoctargetbucketlogs\",\"appUserName\":\"AWSService\",\"time\":{\"timstamp\":1597921835000,\"minOffsetFromGMT\":0,\"minDst\":0},\"sessionLocator\":{\"clientIp\":\"1.1.1.1\",\"clientPort\":0,\"serverIp\":\"2.2.2.2\",\"serverPort\":0,\"isIpv6\":false,\"clientIpv6\":null,\"serverIpv6\":null},\"accessor\":{\"dbUser\":\"AWSService\",\"serverType\":\"S3\",\"serverOs\":\"\",\"clientOs\":\"\",\"clientHostName\":\"cloudtrail.amazonaws.com\",\"serverHostName\":\"s33.amazonaws.com\",\"commProtocol\":\"AwsApiCall\",\"dbProtocol\":\"S3\",\"dbProtocolVersion\":\"1.07\",\"osUser\":\"\",\"sourceProgram\":\"cloudtrail22.amazonaws.com\",\"client_mac\":\"\",\"serverDescription\":\"us-east-1\",\"serviceName\":\"s-east-1\",\"language\":\"FREE_TEXT\",\"dataType\":\"CONSTRUCT\"},\"data\":{\"construct\":{\"sentences\":[{\"verb\":\"PutObject\",\"objects\":[{\"name\":\"ucdoctargetbucketlogs/AWSLogs/987076625343/CloudTrail/eu-west-2/2020/08/20/987076625343_CloudTrail_eu-west-2_20200820T1055Z_eAOjq1eVy9BvU7rJ.json.gz\",\"type\":\"Object\",\"fields\":[],\"schema\":\"\"},{\"name\":\"ucdoctargetbucketlogs\",\"type\":\"Bucket\",\"fields\":[],\"schema\":\"\"}],\"descendants\":[],\"fields\":[]}],\"fullSql\":\"{\\\"eventID\\\":\\\"1b443621-e5a7-432d-a06c-1a9ba479059f\\\",\\\"awsRegion\\\":\\\"us-east-1\\\",\\\"eventCategory\\\":\\\"Data\\\",\\\"eventVersion\\\":\\\"1.07\\\",\\\"responseElements\\\":{\\\"x-amz-server-side-encryption\\\":\\\"AES256\\\"},\\\"sourceIPAddress\\\":\\\"cloudtrail.amazonaws.com\\\",\\\"eventSource\\\":\\\"s3.amazonaws.com\\\",\\\"requestParameters\\\":{\\\"bucketName\\\":\\\"ucdoctargetbucketlogs\\\",\\\"Host\\\":\\\"ucdoctargetbucketlogs.s3.us-east-1.amazonaws.com\\\",\\\"x-amz-acl\\\":\\\"bucket-owner-full-control\\\",\\\"x-amz-server-side-encryption\\\":\\\"AES256\\\",\\\"key\\\":\\\"AWSLogs/987076625343/CloudTrail/eu-west-2/2020/08/20/987076625343_CloudTrail_eu-west-2_20200820T1055Z_eAOjq1eVy9BvU7rJ.json.gz\\\"},\\\"resources\\\":[{\\\"type\\\":\\\"AWS::S3::Object\\\",\\\"ARN\\\":\\\"arn:aws:s3:::ucdoctargetbucketlogs/AWSLogs/987076625343/CloudTrail/eu-west-2/2020/08/20/987076625343_CloudTrail_eu-west-2_20200820T1055Z_eAOjq1eVy9BvU7rJ.json.gz\\\"},{\\\"type\\\":\\\"AWS::S3::Bucket\\\",\\\"accountId\\\":\\\"987076625343\\\",\\\"ARN\\\":\\\"arn:aws:s3:::ucdoctargetbucketlogs\\\"}],\\\"userAgent\\\":\\\"cloudtrail.amazonaws.com\\\",\\\"readOnly\\\":false,\\\"userIdentity\\\":{\\\"invokedBy\\\":\\\"cloudtrail.amazonaws.com\\\",\\\"type\\\":\\\"AWSService\\\"},\\\"eventType\\\":\\\"AwsApiCall\\\",\\\"additionalEventData\\\":{\\\"SignatureVersion\\\":\\\"SigV4\\\",\\\"CipherSuite\\\":\\\"ECDHE-RSA-AES128-SHA\\\",\\\"bytesTransferredIn\\\":765.0,\\\"SSEApplied\\\":\\\"SSE_S3\\\",\\\"AuthenticationMethod\\\":\\\"AuthHeader\\\",\\\"x-amz-id-2\\\":\\\"76JGAE28IdmL+xJ8rrssKzzH2H2Xaj1CovuOGNZ66fqORclykbG6tw5y1IRfMR/IjjxfQ+kn5Jo\\\\u003d\\\",\\\"bytesTransferredOut\\\":0.0},\\\"sharedEventID\\\":\\\"607cf029-2b4a-4782-9dd4-c469ed245314\\\",\\\"requestID\\\":\\\"7857A6C0C78C86BC\\\",\\\"eventTime\\\":\\\"2020-08-20T11:10:35Z\\\",\\\"recipientAccountId\\\":\\\"987076625343\\\",\\\"eventName\\\":\\\"PutObject\\\",\\\"managementEvent\\\":false}\",\"redactedSensitiveDataSql\":\"{\\\"eventID\\\":\\\"1b443621-e5a7-432d-a06c-1a9ba479059f\\\",\\\"awsRegion\\\":\\\"us-east-1\\\",\\\"eventCategory\\\":\\\"Data\\\",\\\"eventVersion\\\":\\\"1.07\\\",\\\"responseElements\\\":{\\\"x-amz-server-side-encryption\\\":\\\"AES256\\\"},\\\"sourceIPAddress\\\":\\\"cloudtrail.amazonaws.com\\\",\\\"eventSource\\\":\\\"s3.amazonaws.com\\\",\\\"requestParameters\\\":{\\\"bucketName\\\":\\\"ucdoctargetbucketlogs\\\",\\\"Host\\\":\\\"ucdoctargetbucketlogs.s3.us-east-1.amazonaws.com\\\",\\\"x-amz-acl\\\":\\\"bucket-owner-full-control\\\",\\\"x-amz-server-side-encryption\\\":\\\"AES256\\\",\\\"key\\\":\\\"AWSLogs/987076625343/CloudTrail/eu-west-2/2020/08/20/987076625343_CloudTrail_eu-west-2_20200820T1055Z_eAOjq1eVy9BvU7rJ.json.gz\\\"},\\\"resources\\\":[{\\\"type\\\":\\\"AWS::S3::Object\\\",\\\"ARN\\\":\\\"arn:aws:s3:::ucdoctargetbucketlogs/AWSLogs/987076625343/CloudTrail/eu-west-2/2020/08/20/987076625343_CloudTrail_eu-west-2_20200820T1055Z_eAOjq1eVy9BvU7rJ.json.gz\\\"},{\\\"type\\\":\\\"AWS::S3::Bucket\\\",\\\"accountId\\\":\\\"987076625343\\\",\\\"ARN\\\":\\\"arn:aws:s3:::ucdoctargetbucketlogs\\\"}],\\\"userAgent\\\":\\\"cloudtrail.amazonaws.com\\\",\\\"readOnly\\\":false,\\\"userIdentity\\\":{\\\"invokedBy\\\":\\\"cloudtrail.amazonaws.com\\\",\\\"type\\\":\\\"AWSService\\\"},\\\"eventType\\\":\\\"AwsApiCall\\\",\\\"additionalEventData\\\":{\\\"SignatureVersion\\\":\\\"SigV4\\\",\\\"CipherSuite\\\":\\\"ECDHE-RSA-AES128-SHA\\\",\\\"bytesTransferredIn\\\":765.0,\\\"SSEApplied\\\":\\\"SSE_S3\\\",\\\"AuthenticationMethod\\\":\\\"AuthHeader\\\",\\\"x-amz-id-2\\\":\\\"76JGAE28IdmL+xJ8rrssKzzH2H2Xaj1CovuOGNZ66fqORclykbG6tw5y1IRfMR/IjjxfQ+kn5Jo\\\\u003d\\\",\\\"bytesTransferredOut\\\":0.0},\\\"sharedEventID\\\":\\\"607cf029-2b4a-4782-9dd4-c469ed245314\\\",\\\"requestID\\\":\\\"7857A6C0C78C86BC\\\",\\\"eventTime\\\":\\\"2020-08-20T11:10:35Z\\\",\\\"recipientAccountId\\\":\\\"987076625343\\\",\\\"eventName\\\":\\\"PutObject\\\",\\\"managementEvent\\\":false}\"},\"originalSqlCommand\":\"{\\\"eventID\\\":\\\"1b443621-e5a7-432d-a06c-1a9ba479059f\\\",\\\"awsRegion\\\":\\\"us-east-1\\\",\\\"eventCategory\\\":\\\"Data\\\",\\\"eventVersion\\\":\\\"1.07\\\",\\\"responseElements\\\":{\\\"x-amz-server-side-encryption\\\":\\\"AES256\\\"},\\\"sourceIPAddress\\\":\\\"cloudtrail.amazonaws.com\\\",\\\"eventSource\\\":\\\"s3.amazonaws.com\\\",\\\"requestParameters\\\":{\\\"bucketName\\\":\\\"ucdoctargetbucketlogs\\\",\\\"Host\\\":\\\"ucdoctargetbucketlogs.s3.us-east-1.amazonaws.com\\\",\\\"x-amz-acl\\\":\\\"bucket-owner-full-control\\\",\\\"x-amz-server-side-encryption\\\":\\\"AES256\\\",\\\"key\\\":\\\"AWSLogs/987076625343/CloudTrail/eu-west-2/2020/08/20/987076625343_CloudTrail_eu-west-2_20200820T1055Z_eAOjq1eVy9BvU7rJ.json.gz\\\"},\\\"resources\\\":[{\\\"type\\\":\\\"AWS::S3::Object\\\",\\\"ARN\\\":\\\"arn:aws:s3:::ucdoctargetbucketlogs/AWSLogs/987076625343/CloudTrail/eu-west-2/2020/08/20/987076625343_CloudTrail_eu-west-2_20200820T1055Z_eAOjq1eVy9BvU7rJ.json.gz\\\"},{\\\"type\\\":\\\"AWS::S3::Bucket\\\",\\\"accountId\\\":\\\"987076625343\\\",\\\"ARN\\\":\\\"arn:aws:s3:::ucdoctargetbucketlogs\\\"}],\\\"userAgent\\\":\\\"cloudtrail.amazonaws.com\\\",\\\"readOnly\\\":false,\\\"userIdentity\\\":{\\\"invokedBy\\\":\\\"cloudtrail.amazonaws.com\\\",\\\"type\\\":\\\"AWSService\\\"},\\\"eventType\\\":\\\"AwsApiCall\\\",\\\"additionalEventData\\\":{\\\"SignatureVersion\\\":\\\"SigV4\\\",\\\"CipherSuite\\\":\\\"ECDHE-RSA-AES128-SHA\\\",\\\"bytesTransferredIn\\\":765.0,\\\"SSEApplied\\\":\\\"SSE_S3\\\",\\\"AuthenticationMethod\\\":\\\"AuthHeader\\\",\\\"x-amz-id-2\\\":\\\"76JGAE28IdmL+xJ8rrssKzzH2H2Xaj1CovuOGNZ66fqORclykbG6tw5y1IRfMR/IjjxfQ+kn5Jo\\\\u003d\\\",\\\"bytesTransferredOut\\\":0.0},\\\"sharedEventID\\\":\\\"607cf029-2b4a-4782-9dd4-c469ed245314\\\",\\\"requestID\\\":\\\"7857A6C0C78C86BC\\\",\\\"eventTime\\\":\\\"2020-08-20T11:10:35Z\\\",\\\"recipientAccountId\\\":\\\"987076625343\\\",\\\"eventName\\\":\\\"PutObject\\\",\\\"managementEvent\\\":false}\",\"useConstruct\":true},\"exception\":null}";

    public static String POSTGRE_record = "{\n" +
            "  \"sessionId\": \"123645\",\n" +
            "  \"dbName\": \"testPOSTGRE\",\n" +
            "  \"appUserName\": \"AWSService\",\n" +
            "  \"time\": {\n" +
            "    \"timstamp\": %d,\n" +
            "    \"minOffsetFromGMT\": 0,\n" +
            "    \"minDst\": 0\n" +
            "  },\n" +
            "  \"sessionLocator\": {\n" +
            "    \"clientIp\": \"183.87.237.%d\",\n" +
            "    \"clientPort\": %d,\n" +
            "    \"serverIp\": \"2.2.2.2\",\n" +
            "    \"serverPort\": 0,\n" +
            "    \"isIpv6\": false,\n" +
            "    \"clientIpv6\": null,\n" +
            "    \"serverIpv6\": null\n" +
            "  },\n" +
            "  \"accessor\": {\n" +
            "    \"dbUser\": \"postgres@testDB\",\n" +
            "    \"serverType\": \"Postgre\",\n" +
            "    \"serverOs\": \"\",\n" +
            "    \"clientOs\": \"\",\n" +
            "    \"clientHostName\": \"we.do.not.have.it.com\",\n" +
            "    \"serverHostName\": \"PPostgres.amazonaws.com\",\n" +
            "    \"commProtocol\": \"AwsApiCall\",\n" +
            "    \"dbProtocol\": \"Postgre AWS Native Audit\",\n" +
            "    \"dbProtocolVersion\": \"1.07\",\n" +
            "    \"osUser\": \"\",\n" +
            "    \"sourceProgram\": \"PostrgeConsole\",\n" +
            "    \"client_mac\": \"\",\n" +
            "    \"serverDescription\": \"mumbai\",\n" +
            "    \"serviceName\": \"mumbai\",\n" +
            "    \"language\": \"PGRS\",\n" +
            "    \"dataType\": \"TEXT\"\n" +
            "  },\n" +
            "  \"data\": {\n" +
            "    \"originalSqlCommand\": \"CREATE TABLE dept(dept_id int PRIMARY KEY,dept_name VARCHAR(50))\",\n" +
            "    \"construct\": null\n" +
            "  },\n" +
            "  \"exception\": null\n" +
            "}";

    public static String mysqlMsg = "{\n" +
            "  \"sessionId\": \"12\",\n" +
            "  \"dbName\": \"\",\n" +
            "  \"appUserName\": \"\",\n" +
            "  \"time\": {\n" +
            "    \"timstamp\": %d,\n" +
            "    \"minOffsetFromGMT\": 0,\n" +
            "    \"minDst\": 0\n" +
            "  },\n" +
            "  \"sessionLocator\": {\n" +
            "    \"clientIp\": \"9.70.165.163\",\n" +
            "    \"clientPort\": 0,\n" +
            "    \"serverIp\": \"9.70.165.163\",\n" +
            "    \"serverPort\": 0,\n" +
            "    \"isIpv6\": false,\n" +
            "    \"clientIpv6\": \"\",\n" +
            "    \"serverIpv6\": \"\"\n" +
            "  },\n" +
            "  \"accessor\": {\n" +
            "    \"dbUser\": \"guardium_qa2\",\n" +
            "    \"serverType\": \"MySql\",\n" +
            "    \"serverOs\": \"\",\n" +
            "    \"clientOs\": \"\",\n" +
            "    \"clientHostName\": \"\",\n" +
            "    \"serverHostName\": \"rh7u4x64t\",\n" +
            "    \"commProtocol\": \"\",\n" +
            "    \"dbProtocol\": \"MySQL native audit\",\n" +
            "    \"dbProtocolVersion\": \"\",\n" +
            "    \"osUser\": \"\",\n" +
            "    \"sourceProgram\": \"mysql_audit_log\",\n" +
            "    \"client_mac\": \"\",\n" +
            "    \"serverDescription\": \"\",\n" +
            "    \"serviceName\": \"\",\n" +
            "    \"language\": \"MYSQL\",\n" +
            "    \"dataType\": \"TEXT\"\n" +
            "  },\n" +
            "  \"data\": null,\n" +
            "  \"exception\": {\n" +
            "    \"exceptionTypeId\": \"SQL_ERROR\",\n" +
            "    \"description\": \"Error (1046)\",\n" +
            "    \"sqlString\": \"select * from nidhi\"\n" +
            "  }\n" +
            "}";
}