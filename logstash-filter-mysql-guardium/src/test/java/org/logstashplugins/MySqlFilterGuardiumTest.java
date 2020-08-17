package org.logstashplugins;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;
import org.junit.Assert;
import org.junit.Test;
import org.logstash.plugins.ConfigurationImpl;
import org.logstash.plugins.ContextImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

public class MySqlFilterGuardiumTest {

    @Test
    public void testParseMySqlSyslog() {
        final String mysql_message = "{ \"timestamp\": \"2020-08-13 14:57:45\", \"id\": 0, \"class\": \"table_access\", \"event\": \"insert\", \"connection_id\": 21, \"account\": { \"user\": \"guardium_qa\", \"host\": \"localhost\" }, \"login\": { \"user\": \"guardium_qa\", \"os\": \"\", \"ip\": \"\", \"proxy\": \"\" }, \"table_access_data\": { \"db\": \"guardium_qa\", \"table\": \"pet\", \"query\": \"SELECT * FROM pet;\", \"sql_command\": \"select\" } },";
        Configuration config = new ConfigurationImpl(Collections.singletonMap("source", "message"));
        Context context = new ContextImpl(null, null);
        MySqlFilterGuardium filter = new MySqlFilterGuardium("test-id", config, context);

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();
        
        e.setField("mysql_message", mysql_message);
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField("Record"));
        Assert.assertEquals(1, matchListener.getMatchCount());
    }

}

class TestMatchListener implements FilterMatchListener {

    private AtomicInteger matchCount = new AtomicInteger(0);

    @Override
    public void filterMatched(Event event) {
        matchCount.incrementAndGet();
    }

    public int getMatchCount() {
        return matchCount.get();
    }
}
