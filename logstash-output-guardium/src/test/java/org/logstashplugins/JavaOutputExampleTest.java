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
        // test based on mongo-guardium filter v0.0.2, which currently sends Event like this: 
/*
        {
            "database" => "config",
                "host" => "9.70.147.59",
      "source_program" => "mongod",
           "Construct" => "{\n  \"sentences\": [\n    {\n      \"verb\": \"find\",\n      \"objects\": [\n        {\n          \"name\": \"transactions\",\n          \"type\": \"collection\",\n          \"fields\": [],\n          \"schema\": \"\"\n        }\n      ],\n      \"descendants\": [],\n      \"fields\": []\n    }\n  ],\n  \"full_sql\": null,\n  \"original_sql\": null\n}",
          "session_id" => "n/a",
             "program" => "mongod",
    "syslog_timestamp" => "Feb 19 05:09:06",
            "@version" => "1",
             "message" => "<14>Feb 19 05:09:06 qa-db51 mongod: { \"atype\" : \"authCheck\", \"ts\" : { \"$date\" : \"2020-02-19T05:09:06.149-0500\" }, \"local\" : { \"ip\" : \"(NONE)\", \"port\" : 0 }, \"remote\" : { \"ip\" : \"(NONE)\", \"port\" : 0 }, \"users\" : [], \"roles\" : [], \"param\" : { \"command\" : \"find\", \"ns\" : \"config.transactions\", \"args\" : { \"find\" : \"transactions\", \"filter\" : { \"lastWriteDate\" : { \"$lt\" : { \"$date\" : \"2020-02-19T04:39:06.148-0500\" } } }, \"projection\" : { \"_id\" : 1 }, \"sort\" : { \"_id\" : 1 }, \"$db\" : \"config\" } }, \"result\" : 0 }",
     "server_hostname" => "qa-db51",
          "@timestamp" => 2020-02-19T10:09:06.149Z,
           "server_ip" => "9.70.147.59",
              "Record" => "{\"sessionId\":\"n/a\",\"dbName\":\"config\",\"appUserName\":\"n/a\",\"time\":0,\"sessionLocator\":{\"clientIp\":\"n/a\",\"clientPort\":0,\"serverIp\":\"9.70.147.59\",\"serverPort\":0,\"isIpv6\":false,\"clientIpv6\":\"n/a\",\"serverIpv6\":\"n/a\"},\"accessor\":{\"dbUser\":\"\",\"serverType\":\"n/a\",\"serverOs\":\"n/a\",\"clientOs\":\"n/a\",\"clientHostName\":\"n/a\",\"serverHostName\":\"qa-db51\",\"commProtocol\":\"n/a\",\"dbProtocol\":\"MONGODB\",\"dbProtocolVersion\":\"n/a\",\"osUser\":\"n/a\",\"sourceProgram\":\"mongod\",\"client_mac\":\"n/a\",\"serverDescription\":\"n/a\",\"serviceName\":\"n/a\",\"language\":\"FREE_TEXT\",\"type\":\"CONSTRUCT\"},\"data\":null}",
         "db_protocol" => "MONGODB",
           "timestamp" => "2020-02-19T05:09:06.149-0500",
         "server_port" => 0,
             "db_user" => "",
      "syslog_message" => "{ \"atype\" : \"authCheck\", \"ts\" : { \"$date\" : \"2020-02-19T05:09:06.149-0500\" }, \"local\" : { \"ip\" : \"(NONE)\", \"port\" : 0 }, \"remote\" : { \"ip\" : \"(NONE)\", \"port\" : 0 }, \"users\" : [], \"roles\" : [], \"param\" : { \"command\" : \"find\", \"ns\" : \"config.transactions\", \"args\" : { \"find\" : \"transactions\", \"filter\" : { \"lastWriteDate\" : { \"$lt\" : { \"$date\" : \"2020-02-19T04:39:06.148-0500\" } } }, \"projection\" : { \"_id\" : 1 }, \"sort\" : { \"_id\" : 1 }, \"$db\" : \"config\" } }, \"result\" : 0 }",
         "server_type" => "MONGODB",
                "type" => "syslog"
}*/
        e.setField("timestamp", "2020-02-19T05:09:06.149-0500");
        e.setField("Record", "{\"sessionId\":\"n/a\",\"dbName\":\"config\",\"appUserName\":\"n/a\",\"time\":0,\"sessionLocator\":{\"clientIp\":\"n/a\",\"clientPort\":0,\"serverIp\":\"9.70.147.59\",\"serverPort\":0,\"isIpv6\":false,\"clientIpv6\":\"n/a\",\"serverIpv6\":\"n/a\"},\"accessor\":{\"dbUser\":\"\",\"serverType\":\"n/a\",\"serverOs\":\"n/a\",\"clientOs\":\"n/a\",\"clientHostName\":\"n/a\",\"serverHostName\":\"qa-db51\",\"commProtocol\":\"n/a\",\"dbProtocol\":\"MONGODB\",\"dbProtocolVersion\":\"n/a\",\"osUser\":\"n/a\",\"sourceProgram\":\"mongod\",\"client_mac\":\"n/a\",\"serverDescription\":\"n/a\",\"serviceName\":\"n/a\",\"language\":\"FREE_TEXT\",\"type\":\"CONSTRUCT\"},\"data\":null}");
        e.setField("Construct", "{\n  \"sentences\": [\n    {\n      \"verb\": \"find\",\n      \"objects\": [\n        {\n          \"name\": \"transactions\",\n          \"type\": \"collection\",\n          \"fields\": [],\n          \"schema\": \"\"\n        }\n      ],\n      \"descendants\": [],\n      \"fields\": []\n    }\n  ],\n  \"full_sql\": null,\n  \"original_sql\": null\n}");
        e.setField("server_type", "MONGODB");
        e.setField("server_hostname", "qa-db51");
        //e.setField("source_program", "mongod");
        //e.setField("session_id", "n/a");
        //e.setField("db_protocol", "MONGODB");
        //e.setField("db_user", ""); // sometimes no user is noted in mongo audit
        
        //e.setField("app_user_name", ""); // in record
        //e.setField("database", "config");
        //e.setField("operation", "find"); // removed in filter
        //e.setField("resource_name", "transactions");
        //e.setField("resource_type", "collection"); // removed in filter
        //e.setField("client_ip", "127.0.0.1");
        // e.setField("client_port", "57610");
        // e.setField("client_hostname", null);
        // e.setField("server_ip", "9.70.147.59");
        // e.setField("server_port", "27017");
        // e.setField("client_os", null);
        // e.setField("server_os", null);
        
        events.add(e);


        output.output(events);

        /*String outputString = baos.toString();
        int index = 0;
        int lastIndex = 0;
        while (index < eventCount) {
            lastIndex = outputString.indexOf(prefix, lastIndex);
            Assert.assertTrue("Prefix should exist in output string", lastIndex > -1);
            lastIndex = outputString.indexOf("message " + index);
            Assert.assertTrue("Message should exist in output string", lastIndex > -1);
            index++;
        }*/
    }
}
