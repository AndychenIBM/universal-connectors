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
        e.setField("db_protocol", "MONGO PROTOCOL");
        e.setField("server_type", "MONGODB");
        e.setField("db_user_name", "Ofer");
        e.setField("app_user_name", "");
        e.setField("timestamp", "2019-11-20T10:30:25.092-0500");
        e.setField("operation", "find");
        e.setField("database", "test");
        e.setField("resource_type", "collection");
        e.setField("resource_name", "people");
        e.setField("client_ip", "127.0.0.1");
        e.setField("client_port", "57610");
        e.setField("client_hostname", null);
        e.setField("server_ip", "127.0.0.1");
        e.setField("server_port", "27017");
        e.setField("source_program", "qa_db51");
        e.setField("client_os", null);
        e.setField("server_os", null);
        e.setField("server_hostname", null);
        e.setField("session_id", "id12345678901111");
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
