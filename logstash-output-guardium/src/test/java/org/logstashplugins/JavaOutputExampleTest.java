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

        int count = 0;
        while (keepGoing /*&& count-->0*/) {
            Date time = new Date();
            int ipSuffix = random.ints(0, 255).findFirst().getAsInt();
            int serverPort = random.ints(0,3).findFirst().getAsInt();
            System.out.println("serverPort "+serverPort);
            String recordTemplate = "{\"sessionId\":\"0vpeEPORRFecjnJihLQbqQ\\u003d\\u003d\",\"dbName\":\"admin\",\"appUserName\":\"\",\"time\":" +
                    +time.getTime()+
                    ",\"sessionLocator\":{\"clientIp\":\"9.42.29."
                    +ipSuffix+
                    "\",\"clientPort\":\"1234\",\"serverIp\":\"9.42.29.56\",\"serverPort\":"
                    +serverPort+
                    ",\"isIpv6\":false,\"clientIpv6\":\"\",\"serverIpv6\":\"\"},\"accessor\":{\"dbUser\":\"Kerri \",\"serverType\":\"MongoDB\",\"serverOs\":\"\",\"clientOs\":\"\",\"clientHostName\":\"\",\"serverHostName\":\"dbqa111\",\"commProtocol\":\"\",\"dbProtocol\":\"MongoDB native audit\",\"dbProtocolVersion\":\"\",\"osUser\":\"\",\"sourceProgram\":\"mongod\",\"client_mac\":\"\",\"serverDescription\":\"\",\"serviceName\":\"admin\",\"language\":\"FREE_TEXT\",\"type\":\"CONSTRUCT\"},\"data\":{\"construct\":{\"sentences\":[{\"verb\":\"create\",\"objects\":[{\"name\":\"customer_A126\",\"type\":\"collection\",\"fields\":[],\"schema\":\"\"}],\"descendants\":[],\"fields\":[]}],\"fullSql\":\"{\\\"atype\\\":\\\"authCheck\\\",\\\"ts\\\":{\\\"$date\\\":\\\"2020-07-20T05:30:02.291-0400\\\"},\\\"local\\\":{\\\"ip\\\":\\\"9.42.29.56\\\",\\\"port\\\":27017},\\\"remote\\\":{\\\"ip\\\":\\\"9.42.29.56\\\",\\\"port\\\":41050},\\\"users\\\":[{\\\"user\\\":\\\"Kerri\\\",\\\"db\\\":\\\"admin\\\"}],\\\"roles\\\":[{\\\"role\\\":\\\"root\\\",\\\"db\\\":\\\"admin\\\"}],\\\"param\\\":{\\\"command\\\":\\\"create\\\",\\\"ns\\\":\\\"admin.customer_A126\\\",\\\"args\\\":{\\\"create\\\":\\\"customer_A126\\\",\\\"lsid\\\":{\\\"id\\\":{\\\"$binary\\\":\\\"0vpeEPORRFecjnJihLQbqQ\\u003d\\u003d\\\",\\\"$type\\\":\\\"04\\\"}},\\\"$db\\\":\\\"admin\\\",\\\"$readPreference\\\":{\\\"mode\\\":\\\"primary\\\"}}},\\\"result\\\":0}\",\"original_sql\":\"{\\\"atype\\\":\\\"authCheck\\\",\\\"ts\\\":{\\\"$date\\\":\\\"2020-07-20T05:30:02.291-0400\\\"},\\\"local\\\":{\\\"ip\\\":\\\"9.42.29.56\\\",\\\"port\\\":27017},\\\"remote\\\":{\\\"ip\\\":\\\"9.42.29.56\\\",\\\"port\\\":41050},\\\"users\\\":[{\\\"user\\\":\\\"Kerri\\\",\\\"db\\\":\\\"admin\\\"}],\\\"roles\\\":[{\\\"role\\\":\\\"root\\\",\\\"db\\\":\\\"admin\\\"}],\\\"param\\\":{\\\"command\\\":\\\"create\\\",\\\"ns\\\":\\\"admin.customer_A126\\\",\\\"args\\\":{\\\"lsid\\\":{\\\"id\\\":{\\\"$binary\\\":\\\"?\\\",\\\"$type\\\":\\\"?\\\"}},\\\"$readPreference\\\":{\\\"mode\\\":\\\"?\\\"},\\\"create\\\":\\\"customer_A126\\\",\\\"$db\\\":\\\"admin\\\"}},\\\"result\\\":0}\"},\"timestamp\":1595237402291,\"originalSqlCommand\":\"\",\"useConstruct\":true},\"exception\":null}";
            // test based on mongo-guardium filter v0.0.2, which currently sends Event like this:
            e.setField("Record", recordTemplate);
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
        e.setField("Record", "{\"sessionId\":\"0vpeEPORRFecjnJihLQbqQ\\u003d\\u003d\",\"dbName\":\"admin\",\"appUserName\":\"\",\"time\":1595237402291,\"sessionLocator\":{\"clientIp\":\"9.42.29.56\",\"clientPort\":41050,\"serverIp\":\"9.42.29.56\",\"serverPort\":27017,\"isIpv6\":false,\"clientIpv6\":\"\",\"serverIpv6\":\"\"},\"accessor\":{\"dbUser\":\"Kerri \",\"serverType\":\"MongoDB\",\"serverOs\":\"\",\"clientOs\":\"\",\"clientHostName\":\"\",\"serverHostName\":\"dbqa111\",\"commProtocol\":\"\",\"dbProtocol\":\"MongoDB native audit\",\"dbProtocolVersion\":\"\",\"osUser\":\"\",\"sourceProgram\":\"mongod\",\"client_mac\":\"\",\"serverDescription\":\"\",\"serviceName\":\"admin\",\"language\":\"FREE_TEXT\",\"type\":\"CONSTRUCT\"},\"data\":{\"construct\":{\"sentences\":[{\"verb\":\"create\",\"objects\":[{\"name\":\"customer_A126\",\"type\":\"collection\",\"fields\":[],\"schema\":\"\"}],\"descendants\":[],\"fields\":[]}],\"full_sql\":\"{\\\"atype\\\":\\\"authCheck\\\",\\\"ts\\\":{\\\"$date\\\":\\\"2020-07-20T05:30:02.291-0400\\\"},\\\"local\\\":{\\\"ip\\\":\\\"9.42.29.56\\\",\\\"port\\\":27017},\\\"remote\\\":{\\\"ip\\\":\\\"9.42.29.56\\\",\\\"port\\\":41050},\\\"users\\\":[{\\\"user\\\":\\\"Kerri\\\",\\\"db\\\":\\\"admin\\\"}],\\\"roles\\\":[{\\\"role\\\":\\\"root\\\",\\\"db\\\":\\\"admin\\\"}],\\\"param\\\":{\\\"command\\\":\\\"create\\\",\\\"ns\\\":\\\"admin.customer_A126\\\",\\\"args\\\":{\\\"create\\\":\\\"customer_A126\\\",\\\"lsid\\\":{\\\"id\\\":{\\\"$binary\\\":\\\"0vpeEPORRFecjnJihLQbqQ\\u003d\\u003d\\\",\\\"$type\\\":\\\"04\\\"}},\\\"$db\\\":\\\"admin\\\",\\\"$readPreference\\\":{\\\"mode\\\":\\\"primary\\\"}}},\\\"result\\\":0}\",\"original_sql\":\"{\\\"atype\\\":\\\"authCheck\\\",\\\"ts\\\":{\\\"$date\\\":\\\"2020-07-20T05:30:02.291-0400\\\"},\\\"local\\\":{\\\"ip\\\":\\\"9.42.29.56\\\",\\\"port\\\":27017},\\\"remote\\\":{\\\"ip\\\":\\\"9.42.29.56\\\",\\\"port\\\":41050},\\\"users\\\":[{\\\"user\\\":\\\"Kerri\\\",\\\"db\\\":\\\"admin\\\"}],\\\"roles\\\":[{\\\"role\\\":\\\"root\\\",\\\"db\\\":\\\"admin\\\"}],\\\"param\\\":{\\\"command\\\":\\\"create\\\",\\\"ns\\\":\\\"admin.customer_A126\\\",\\\"args\\\":{\\\"lsid\\\":{\\\"id\\\":{\\\"$binary\\\":\\\"?\\\",\\\"$type\\\":\\\"?\\\"}},\\\"$readPreference\\\":{\\\"mode\\\":\\\"?\\\"},\\\"create\\\":\\\"customer_A126\\\",\\\"$db\\\":\\\"admin\\\"}},\\\"result\\\":0}\"},\"timestamp\":1595237402291,\"originalSqlCommand\":\"\",\"useConstruct\":true},\"exception\":null}");
        e.setField("server_type", "MONGODB");
        e.setField("server_hostname", "qa-db51");

        events.add(e);


        output.output(events);

    }
}
