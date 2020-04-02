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

import com.google.gson.Gson;
import com.ibm.guardium.connector.structures.Record;

public class JavaFilterExampleTest {

    final static String mongodString = "<14>Feb 18 08:53:31 qa-db51 mongod: { \"atype\" : \"authCheck\", \"ts\" : { \"$date\" : \"2020-01-16T05:41:30.783-0500\" }, \"local\" : { \"ip\" : \"(NONE)\", \"port\" : 0 }, \"remote\" : { \"ip\" : \"(NONE)\", \"port\" : 0 }, \"users\" : [], \"roles\" : [], \"param\" : { \"command\" : \"find\", \"ns\" : \"config.transactions\", \"args\" : { \"find\" : \"transactions\", \"filter\" : { \"lastWriteDate\" : { \"$lt\" : { \"$date\" : \"2020-01-16T05:11:30.782-0500\" } } }, \"projection\" : { \"_id\" : 1 }, \"sort\" : { \"_id\" : 1 }, \"$db\" : \"config\" } }, \"result\" : 0 }";
    final static Context context = new ContextImpl(null, null);
    final static JavaFilterExample filter = new JavaFilterExample("test-id", null, context);
    /*@Test
    public void testJavaExampleFilter() {
        String sourceField = "foo";
        Configuration config = new ConfigurationImpl(Collections.singletonMap("source", sourceField));
        Context context = new ContextImpl(null, null);
        JavaFilterExample filter = new JavaFilterExample("test-id", config, context);

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();
        e.setField(sourceField, "abcdef");
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertEquals("fedcba", e.getField(sourceField));
        Assert.assertEquals(1, matchListener.getMatchCount());
    } */

    @Test
    public void testParseMongoSyslog() {
        String mongodString = "<14>Feb 18 08:53:31 qa-db51 mongod: { \"atype\" : \"authCheck\", \"ts\" : { \"$date\" : \"2020-01-16T05:41:30.783-0500\" }, \"local\" : { \"ip\" : \"(NONE)\", \"port\" : 0 }, \"remote\" : { \"ip\" : \"(NONE)\", \"port\" : 0 }, \"users\" : [], \"roles\" : [], \"param\" : { \"command\" : \"find\", \"ns\" : \"config.transactions\", \"args\" : { \"find\" : \"transactions\", \"filter\" : { \"lastWriteDate\" : { \"$lt\" : { \"$date\" : \"2020-01-16T05:11:30.782-0500\" } } }, \"projection\" : { \"_id\" : 1 }, \"sort\" : { \"_id\" : 1 }, \"$db\" : \"config\" } }, \"result\" : 0 }";
        Context context = new ContextImpl(null, null);
        JavaFilterExample filter = new JavaFilterExample("test-id", null, context);

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();
        
        e.setField("message", mongodString);
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertNotNull(e.getField("Record"));
        Assert.assertEquals(1, matchListener.getMatchCount());
    }
    

    @Test 
    public void testParseOtherSyslog() {
        String syslogString = "<7>Feb 18 08:55:14 qa-db51 kernel: IPv6 addrconf: prefix with wrong length 96";
        Context context = new ContextImpl(null, null);
        JavaFilterExample filter = new JavaFilterExample("test-id", null, context);

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();
        
        e.setField("message", syslogString);
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertEquals(true,
            e.getField("tags").toString().contains("_mongoguardium_skip"));
        //Assert.assertNull(e.getField("Construct"));
        Assert.assertEquals(0, matchListener.getMatchCount());
    }

    @Test
    public void testParseMongoSyslog_injectHost() {
        final String hostString = "9.9.9.9";

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();
        
        e.setField("message", mongodString);
        e.setField("host", hostString);

        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        String recordString = e.getField("Record").toString();
        Record record = (new Gson()).fromJson(recordString, Record.class);
        Assert.assertNotNull(record);

        Assert.assertEquals(
                "host should be used to override client IP when native audit shows same ip for local & remote",
                hostString, record.getSessionLocator().getClientIp());
        Assert.assertEquals(
                "host should be used to override server IP when native audit shows same ip for local & remote",
                hostString, record.getSessionLocator().getServerIp());
    }

    @Test
    public void testParseMongoSyslog_doNotInjectHost() {
        // syslog message uses different IPs for local/remote:
        final String mongodString = "<14>Feb 18 08:53:31 qa-db51 mongod: { \"atype\" : \"authCheck\", \"ts\" : { \"$date\" : \"2020-01-16T05:41:30.783-0500\" }, \"local\" : { \"ip\" : \"1.2.3.456\", \"port\" : 0 }, \"remote\" : { \"ip\" : \"1.2.3.123\", \"port\" : 0 }, \"users\" : [], \"roles\" : [], \"param\" : { \"command\" : \"find\", \"ns\" : \"config.transactions\", \"args\" : { \"find\" : \"transactions\", \"filter\" : { \"lastWriteDate\" : { \"$lt\" : { \"$date\" : \"2020-01-16T05:11:30.782-0500\" } } }, \"projection\" : { \"_id\" : 1 }, \"sort\" : { \"_id\" : 1 }, \"$db\" : \"config\" } }, \"result\" : 0 }";
        final String hostString = "9.9.9.9";

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();
        
        e.setField("message", mongodString);
        e.setField("host", hostString);

        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        String recordString = e.getField("Record").toString();
        Record record = (new Gson()).fromJson(recordString, Record.class);
        Assert.assertNotNull(record);

        Assert.assertEquals(
                "host should not override client IP when native audit shows different IPs for local & remote",
                "1.2.3.123", record.getSessionLocator().getClientIp());
        Assert.assertEquals(
                "host should not override server IP when native audit shows different IPs for local & remote",
                "1.2.3.456", record.getSessionLocator().getServerIp());
    }


    @Test 
    public void testTagParseError() {
        Assert.assertFalse(true);
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