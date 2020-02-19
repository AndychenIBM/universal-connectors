package org.logstashplugins;

import co.elastic.logstash.api.Event;

import org.junit.Assert;
import org.junit.Test;
import com.ibm.guardium.universalconnector.transformer.jsonrecord.Record;

public class JsonFromEventBuilderTest {

    @Test 
    public void testBuildRecord() throws java.text.ParseException {
        // test based on mongo-guardium filter v0.0.2, which currently sends Event like this: 
        Event e = new org.logstash.Event();
        e.setField("timestamp", "2020-02-19T05:09:06.149-0500");
        e.setField("Record", "{\"sessionId\":\"n/a\",\"dbName\":\"config\",\"appUserName\":\"n/a\",\"time\":0,\"sessionLocator\":{\"clientIp\":\"n/a\",\"clientPort\":0,\"serverIp\":\"9.70.147.59\",\"serverPort\":0,\"isIpv6\":false,\"clientIpv6\":\"n/a\",\"serverIpv6\":\"n/a\"},\"accessor\":{\"dbUser\":\"\",\"serverType\":\"n/a\",\"serverOs\":\"n/a\",\"clientOs\":\"n/a\",\"clientHostName\":\"n/a\",\"serverHostName\":\"qa-db51\",\"commProtocol\":\"n/a\",\"dbProtocol\":\"MONGODB\",\"dbProtocolVersion\":\"n/a\",\"osUser\":\"n/a\",\"sourceProgram\":\"mongod\",\"client_mac\":\"n/a\",\"serverDescription\":\"n/a\",\"serviceName\":\"n/a\",\"language\":\"FREE_TEXT\",\"type\":\"CONSTRUCT\"},\"data\":null}");
        e.setField("Construct", "{\n  \"sentences\": [\n    {\n      \"verb\": \"find\",\n      \"objects\": [\n        {\n          \"name\": \"transactions\",\n          \"type\": \"collection\",\n          \"fields\": [],\n          \"schema\": \"\"\n        }\n      ],\n      \"descendants\": [],\n      \"fields\": []\n    }\n  ],\n  \"full_sql\": null,\n  \"original_sql\": null\n}");
        e.setField("server_type", "MONGODB");
        e.setField("server_hostname", "qa-db51");
        
        JsonFromEventBuilder builder = new JsonFromEventBuilder();
        Record actualRecord = builder.buildRecord(e);
        Assert.assertNotNull(actualRecord);
    }
}
