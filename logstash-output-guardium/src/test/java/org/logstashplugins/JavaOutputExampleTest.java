package org.logstashplugins;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Event;
import com.ibm.guardium.universalconnector.transformer.JsonRecordTransformer;
import org.junit.Test;
import org.logstash.plugins.ConfigurationImpl;
import org.omg.CORBA.UNKNOWN;
import sun.net.util.IPAddressUtil;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.*;

public class JavaOutputExampleTest {

//    @Test
    public void testJavaOutputExample() throws InterruptedException {
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
        while (keepGoing /*&& count++<30*/) {
            Date time = new Date();
            int ipSuffix = random.ints(0, 255).findFirst().getAsInt();
            int serverPort = random.ints(0,3).findFirst().getAsInt();
            System.out.println("serverPort "+serverPort);
            System.out.println("    clientIp 9.42.29."+ipSuffix);

            // test based on mongo-guardium filter v0.0.2, which currently sends Event like this:
            String record = String.format(msgTemplate, time.getTime(), ipSuffix, serverPort);
            e.setField("Record", record);
            events.add(e);

            output.output(events);
            Thread.sleep(10000);
        }
    }

    @Test
    public void testJavaOutputExceptionExample() {
        String prefix = "Prefix";
        Map<String, Object> configValues = new HashMap<>();
        configValues.put(JavaOutputToGuardium.PREFIX_CONFIG.name(), prefix);
        Configuration config = new ConfigurationImpl(configValues);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setProperty("UC_ETC", "C:\\Guard\\Git\\universal-connector\\logstash-output-guardium\\src\\resources\\");
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

    @Test
    public void testJavaOutputExample2() throws InterruptedException {
        String prefix = "Prefix";
        Map<String, Object> configValues = new HashMap<>();
        configValues.put(JavaOutputToGuardium.PREFIX_CONFIG.name(), prefix);
        Configuration config = new ConfigurationImpl(configValues);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JavaOutputToGuardium output = new JavaOutputToGuardium("test-id", config, null, baos);

        Collection<Event> events = new ArrayList<>();

        Event e = new org.logstash.Event();
        String recordTemplate = msg1;
        e.setField("Record", recordTemplate);
        events.add(e);

        output.output(events);
    }

    public static final String msgTemplate="{\n" +
            "\t\"sessionId\": \"\",\n" +
            "\t\"dbName\": \"admin\",\n" +
            "\t\"appUserName\": \"\",\n" +
            "\t\"time\": %d,\n" +
            "\t\"sessionLocator\": {\n" +
            "\t\t\"clientIp\": \"1.1.1.%d\",\n" +
            "\t\t\"clientPort\": 36802,\n" +
            "\t\t\"serverIp\": \"9.98.169.111\",\n" +
            "\t\t\"serverPort\": %d,\n" +
            "\t\t\"isIpv6\": false,\n" +
            "\t\t\"clientIpv6\": \"\",\n" +
            "\t\t\"serverIpv6\": \"\"\n" +
            "\t},\n" +
            "\t\"accessor\": {\n" +
            "\t\t\"dbUser\": \"admin \",\n" +
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
            "\t\t\"type\": \"CONSTRUCT\"\n" +
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


    public static final String msg1="{\n" +
            "\t\"sessionId\": \"testsessionid\",\n" +
            "\t\"dbName\": \"admin\",\n" +
            "\t\"appUserName\": \"\",\n" +
            "\t\"time\": 1589439386518,\n" +
            "\t\"sessionLocator\": {\n" +
            "\t\t\"clientIp\": \"1.1.1.1\",\n" +
            "\t\t\"clientPort\": 36802,\n" +
            "\t\t\"serverIp\": \"2.2.2.2\",\n" +
            "\t\t\"serverPort\": 27017,\n" +
            "\t\t\"isIpv6\": false,\n" +
            "\t\t\"clientIpv6\": \"\",\n" +
            "\t\t\"serverIpv6\": \"\"\n" +
            "\t},\n" +
            "\t\"accessor\": {\n" +
            "\t\t\"dbUser\": \"admin \",\n" +
            "\t\t\"serverType\": \"MongoDB\",\n" +
            "\t\t\"serverOs\": \"\",\n" +
            "\t\t\"clientOs\": \"\",\n" +
            "\t\t\"clientHostName\": \"\",\n" +
            "\t\t\"serverHostName\": \"NutanExample\",\n" +
            "\t\t\"commProtocol\": \"\",\n" +
            "\t\t\"dbProtocol\": \"MongoDB native audit\",\n" +
            "\t\t\"dbProtocolVersion\": \"\",\n" +
            "\t\t\"osUser\": \"\",\n" +
            "\t\t\"sourceProgram\": \"mongod\",\n" +
            "\t\t\"client_mac\": \"\",\n" +
            "\t\t\"serverDescription\": \"\",\n" +
            "\t\t\"serviceName\": \"admin\",\n" +
            "\t\t\"language\": \"FREE_TEXT\",\n" +
            "\t\t\"type\": \"CONSTRUCT\"\n" +
            "\t},\n" +
            "\t\"data\": {\n" +
            "\t\t\"construct\": {\n" +
            "\t\t\t\"sentences\": [\n" +
            "\t\t\t\t{\n" +
            "\t\t\t\t\t\"verb\": \"insert\",\n" +
            "\t\t\t\t\t\"objects\": [\n" +
            "\t\t\t\t\t\t{\n" +
            "\t\t\t\t\t\t\t\"name\": \"system.users\",\n" +
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
