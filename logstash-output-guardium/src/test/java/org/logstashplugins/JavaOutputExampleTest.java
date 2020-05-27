package org.logstashplugins;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Event;
import org.junit.Test;
import org.logstash.plugins.ConfigurationImpl;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class JavaOutputExampleTest {

    @Test
    public void testJavaOutputExample() {
        String prefix = "Prefix";
        Map<String, Object> configValues = new HashMap<>();
        configValues.put(JavaOutputToGuardium.PREFIX_CONFIG.name(), prefix);
        Configuration config = new ConfigurationImpl(configValues);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JavaOutputToGuardium output = new JavaOutputToGuardium("test-id", config, null, baos);

        Collection<Event> events = new ArrayList<>();

        Event e = new org.logstash.Event();
        // test based on mongo-guardium filter v0.0.2, which currently sends Event like this: 
        e.setField("timestamp", "2020-05-18T12:09:06.149-0500");
        e.setField("Record", "{\"sessionId\":\"n/a\",\"dbName\":\"config\",\"appUserName\":\"n/a\",\"time\":1589703915,\"sessionLocator\":{\"clientIp\":\"9.70.147.59\",\"clientPort\":0,\"serverIp\":\"9.70.147.59\",\"serverPort\":27101,\"isIpv6\":false,\"clientIpv6\":\"n/a\",\"serverIpv6\":\"n/a\"},\"accessor\":{\"dbUser\":\"\",\"serverType\":\"MONGODB\",\"serverOs\":\"n/a\",\"clientOs\":\"n/a\",\"clientHostName\":\"n/a\",\"serverHostName\":\"qa-db511\",\"commProtocol\":\"n/a\",\"dbProtocol\":\"Logstash\",\"dbProtocolVersion\":\"n/a\",\"osUser\":\"n/a\",\"sourceProgram\":\"mongod\",\"client_mac\":\"n/a\",\"serverDescription\":\"n/a\",\"serviceName\":\"config\",\"language\":\"FREE_TEXT\",\"type\":\"CONSTRUCT\"},\"data\":{\"construct\":{\"sentences\":[{\"verb\":\"find\",\"objects\":[{\"name\":\"transactions\",\"type\":\"collection\",\"fields\":[],\"schema\":\"\"}],\"descendants\":[],\"fields\":[]}],\"full_sql\":\"{\\\"atype\\\":\\\"authCheck\\\",\\\"ts\\\":{\\\"$date\\\":\\\"2020-05-17T04:25:15.376-0400\\\"},\\\"local\\\":{\\\"ip\\\":\\\"(NONE)\\\",\\\"port\\\":0},\\\"remote\\\":{\\\"ip\\\":\\\"(NONE)\\\",\\\"port\\\":0},\\\"users\\\":[],\\\"roles\\\":[],\\\"param\\\":{\\\"command\\\":\\\"find\\\",\\\"ns\\\":\\\"config.transactions\\\",\\\"args\\\":{\\\"find\\\":\\\"transactions\\\",\\\"filter\\\":{\\\"lastWriteDate\\\":{\\\"$lt\\\":{\\\"$date\\\":\\\"2020-05-17T03:55:15.376-0400\\\"}}},\\\"projection\\\":{\\\"_id\\\":1},\\\"sort\\\":{\\\"_id\\\":1},\\\"$db\\\":\\\"config\\\"}},\\\"result\\\":0}\",\"original_sql\":\"{\\\"atype\\\":\\\"authCheck\\\",\\\"ts\\\":{\\\"$date\\\":\\\"2020-05-17T04:25:15.376-0400\\\"},\\\"local\\\":{\\\"ip\\\":\\\"(NONE)\\\",\\\"port\\\":0},\\\"remote\\\":{\\\"ip\\\":\\\"(NONE)\\\",\\\"port\\\":0},\\\"users\\\":[],\\\"roles\\\":[],\\\"param\\\":{\\\"command\\\":\\\"find\\\",\\\"ns\\\":\\\"config.transactions\\\",\\\"args\\\":{\\\"filter\\\":{\\\"lastWriteDate\\\":{\\\"$lt\\\":{\\\"$date\\\":\\\"?\\\"}}},\\\"projection\\\":{\\\"_id\\\":\\\"?\\\"},\\\"sort\\\":{\\\"_id\\\":\\\"?\\\"},\\\"find\\\":\\\"transactions\\\",\\\"$db\\\":\\\"config\\\"}},\\\"result\\\":0}\"},\"timestamp\":1589703915,\"originalSqlCommand\":\"n/a\",\"useConstruct\":true}}");
        e.setField("server_type", "MONGODB");
        e.setField("server_hostname", "qa-db51");
        events.add(e);

        output.output(events);
    }

    @Test
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
        e.setField("Record", "{\"sessionId\":\"n/a\",\"dbName\":\"configKokoExp\",\"appUserName\":\"n/a\",\"time\":0,\"sessionLocator\":{\"clientIp\":\"n/a\",\"clientPort\":0,\"serverIp\":\"9.70.147.59\",\"serverPort\":0,\"isIpv6\":false,\"clientIpv6\":\"n/a\",\"serverIpv6\":\"n/a\"},\"accessor\":{\"dbUser\":\"kokoexp\",\"serverType\":\"n/a\",\"serverOs\":\"n/a\",\"clientOs\":\"n/a\",\"clientHostName\":\"n/a\",\"serverHostName\":\"qa-db51\",\"commProtocol\":\"n/a\",\"dbProtocol\":\"MONGODB\",\"dbProtocolVersion\":\"n/a\",\"osUser\":\"n/a\",\"sourceProgram\":\"mongod\",\"client_mac\":\"n/a\",\"serverDescription\":\"n/a\",\"serviceName\":\"n/a\",\"language\":\"FREE_TEXT\",\"type\":\"CONSTRUCT\"},\"data\":{\"originalSqlCommand\":\"select * from sales\"},\"exception\":{\"exceptionTypeId\":\"SQL_ERROR\",\"description\":\"parse error\",\"sqlString\":\"select from sales\",\"timestamp\":\"1588573299000\"}}");
        e.setField("Construct", "{\n  \"sentences\": [\n    {\n      \"verb\": \"find\",\n      \"objects\": [\n        {\n          \"name\": \"transactions\",\n          \"type\": \"collection\",\n          \"fields\": [],\n          \"schema\": \"\"\n        }\n      ],\n      \"descendants\": [],\n      \"fields\": []\n    }\n  ],\n  \"full_sql\": null,\n  \"original_sql\": null\n}");
        e.setField("server_type", "MONGODB");
        e.setField("server_hostname", "qa-db51");

        events.add(e);


        output.output(events);

    }
}
