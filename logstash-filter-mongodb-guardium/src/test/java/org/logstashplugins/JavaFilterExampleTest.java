package org.logstashplugins;

//import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.FilterMatchListener;
import org.junit.Assert;
import org.junit.Test;
//import org.logstash.plugins.ConfigurationImpl;
import org.logstash.plugins.ContextImpl;

import java.util.ArrayList;
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
        final String mongodString = "<14>Feb 18 08:53:31 qa-db51 mongod: { \"atype\" : \"authCheck\", \"ts\" : { \"$date\" : \"2020-06-11T09:44:11.070-0400\" }, \"local\" : { \"ip\" : \"9.70.147.59\", \"port\" : 27017 }, \"remote\" : { \"ip\" : \"9.148.202.94\", \"port\" : 60185 }, \"users\" : [ { \"user\" : \"realAdmin\", \"db\" : \"admin\" } ], \"roles\" : [ { \"role\" : \"readWriteAnyDatabase\", \"db\" : \"admin\" }, { \"role\" : \"userAdminAnyDatabase\", \"db\" : \"admin\" } ], \"param\" : { \"command\" : \"find\", \"ns\" : \"admin.USERS\", \"args\" : { \"find\" : \"USERS\", \"filter\" : {}, \"lsid\" : { \"id\" : { \"$binary\" : \"mV20eHvvRha2ELTeqJxQJg==\", \"$type\" : \"04\" } }, \"$db\" : \"admin\", \"$readPreference\" : { \"mode\" : \"primaryPreferred\" } } }, \"result\" : 0 }";
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

    /**
     * Tests that messages are skipped & removed if atype != "authCheck"
     */
    @Test 
    public void testParseMongo_skip_remove_atype_createCollection() {
        String messageString = "<14>Feb 18 08:53:31 qa-db51 mongod: { \"atype\" : \"createCollection\", \"ts\" : { \"$date\" : \"2020-06-03T03:40:30.888-0400\" }, \"local\" : { \"ip\" : \"127.0.0.1\", \"port\" : 27017 }, \"remote\" : { \"ip\" : \"127.0.0.1\", \"port\" : 40426 }, \"users\" : [ { \"user\" : \"realAdmin\", \"db\" : \"admin\" } ], \"roles\" : [ { \"role\" : \"readWriteAnyDatabase\", \"db\" : \"admin\" }, { \"role\" : \"userAdminAnyDatabase\", \"db\" : \"admin\" } ], \"param\" : { \"ns\" : \"newDB01.newCollection01\" }, \"result\" : 0 }";
        Context context = new ContextImpl(null, null);
        JavaFilterExample filter = new JavaFilterExample("test-id", null, context);

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();
        ArrayList<Event> events = new ArrayList<>();
        e.setField("message", messageString);
        events.add(e);
        
        Collection<Event> results = filter.filter(events, matchListener);

        Assert.assertEquals(0, results.size());
        Assert.assertEquals(0, matchListener.getMatchCount());
    }

    /**
     * Tests that messages without identified users are skipped & removed
     */
    @Test 
    public void testParseMongo_skip_remove_empty_users() {
        String messageString = "<14>Feb 18 08:53:31 qa-db51 mongod: { \"atype\" : \"authCheck\", \"ts\" : { \"$date\" : \"2020-06-11T09:50:21.485-0400\" }, \"local\" : { \"ip\" : \"9.70.147.59\", \"port\" : 27017 }, \"remote\" : { \"ip\" : \"9.148.202.94\", \"port\" : 60241 }, \"users\" : [], \"roles\" : [], \"param\" : { \"command\" : \"isMaster\", \"ns\" : \"admin\", \"args\" : { \"ismaster\" : 1, \"client\" : { \"driver\" : { \"name\" : \"PyMongo\", \"version\" : \"3.10.1\" }, \"os\" : { \"type\" : \"Darwin\", \"name\" : \"Darwin\", \"architecture\" : \"x86_64\", \"version\" : \"10.14.6\" }, \"platform\" : \"CPython 2.7.15.final.0\" }, \"$db\" : \"admin\" } }, \"result\" : 0 }";
        Context context = new ContextImpl(null, null);
        JavaFilterExample filter = new JavaFilterExample("test-id", null, context);

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();
        ArrayList<Event> events = new ArrayList<>();
        e.setField("message", messageString);
        events.add(e);
        
        Collection<Event> results = filter.filter(events, matchListener);

        Assert.assertEquals(0, results.size());
        Assert.assertEquals(0, matchListener.getMatchCount());
    }

    /**
     * Tests that atype="authenticate" messages are skipped & removed.
     * 
     * Unsuccessful messages are handled as reported as Exception (Failed login in Guardium).
     */
    @Test 
    public void testParseMongo_skip_remove_atype_authenticate_successful() {
        String messageString = "<14>Feb 18 08:53:31 qa-db51 mongod: { \"atype\" : \"authenticate\", \"ts\" : { \"$date\" : \"2020-06-09T08:34:12.424-0400\" }, \"local\" : { \"ip\" : \"9.70.147.59\", \"port\" : 27017 }, \"remote\" : { \"ip\" : \"9.148.206.148\", \"port\" : 49712 }, \"users\" : [ { \"user\" : \"realAdmin\", \"db\" : \"admin\" } ], \"roles\" : [ { \"role\" : \"readWriteAnyDatabase\", \"db\" : \"admin\" }, { \"role\" : \"userAdminAnyDatabase\", \"db\" : \"admin\" } ], \"param\" : { \"user\" : \"realAdmin\", \"db\" : \"admin\", \"mechanism\" : \"SCRAM-SHA-256\" }, \"result\" : 0 }";
        Context context = new ContextImpl(null, null);
        JavaFilterExample filter = new JavaFilterExample("test-id", null, context);

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();
        ArrayList<Event> events = new ArrayList<>();
        e.setField("message", messageString);
        events.add(e);
        
        Collection<Event> results = filter.filter(events, matchListener);

        Assert.assertEquals(0, results.size());
        Assert.assertEquals(0, matchListener.getMatchCount());
    }

    /**
     * Test integrity of events collection, after skipped events were removed from it.
     * 
     * Tests also that authentication failure is handled and not removed, even though empty users[].
     */
    @Test 
    public void testParseMongo_eventsCollectionIntegrity() {
        String messageStringOK = "<14>Feb 18 08:53:31 qa-db51 mongod: { \"atype\" : \"authCheck\", \"ts\" : { \"$date\" : \"2020-06-11T09:44:11.070-0400\" }, \"local\" : { \"ip\" : \"9.70.147.59\", \"port\" : 27017 }, \"remote\" : { \"ip\" : \"9.148.202.94\", \"port\" : 60185 }, \"users\" : [ { \"user\" : \"realAdmin\", \"db\" : \"admin\" } ], \"roles\" : [ { \"role\" : \"readWriteAnyDatabase\", \"db\" : \"admin\" }, { \"role\" : \"userAdminAnyDatabase\", \"db\" : \"admin\" } ], \"param\" : { \"command\" : \"find\", \"ns\" : \"admin.USERS\", \"args\" : { \"find\" : \"USERS\", \"filter\" : {}, \"lsid\" : { \"id\" : { \"$binary\" : \"mV20eHvvRha2ELTeqJxQJg==\", \"$type\" : \"04\" } }, \"$db\" : \"admin\", \"$readPreference\" : { \"mode\" : \"primaryPreferred\" } } }, \"result\" : 0 }";
        String messageStringSkip = "<14>Feb 18 08:53:32 qa-db51 mongod: { \"atype\" : \"createCollection\", \"ts\" : { \"$date\" : \"2020-06-03T03:40:30.888-0400\" }, \"local\" : { \"ip\" : \"127.0.0.1\", \"port\" : 27017 }, \"remote\" : { \"ip\" : \"127.0.0.1\", \"port\" : 40426 }, \"users\" : [ { \"user\" : \"realAdmin\", \"db\" : \"admin\" } ], \"roles\" : [ { \"role\" : \"readWriteAnyDatabase\", \"db\" : \"admin\" }, { \"role\" : \"userAdminAnyDatabase\", \"db\" : \"admin\" } ], \"param\" : { \"ns\" : \"newDB01.newCollection01\" }, \"result\" : 0 }";
        String messageStringAuthOK = "<14>Feb 18 08:53:33 qa-db51 mongod: { \"atype\" : \"authenticate\", \"ts\" : { \"$date\" : \"2020-05-17T11:37:30.421-0400\" }, \"local\" : { \"ip\" : \"127.0.0.1\", \"port\" : 27017 }, \"remote\" : { \"ip\" : \"127.0.0.1\", \"port\" : 29398 }, \"users\" : [], \"roles\" : [], \"param\" : { \"user\" : \"readerUser\", \"db\" : \"admin\", \"mechanism\" : \"SCRAM-SHA-256\" }, \"result\" : 18 }";
        Context context = new ContextImpl(null, null);
        JavaFilterExample filter = new JavaFilterExample("test-id", null, context);

        TestMatchListener matchListener = new TestMatchListener();
        ArrayList<Event> inputEvents = new ArrayList<>();
        Event e = new org.logstash.Event();
        e.setField("message", messageStringOK);
        Event eSkip = new org.logstash.Event();
        eSkip.setField("message", messageStringSkip);
        Event eAuth = new org.logstash.Event();
        eAuth.setField("message", messageStringAuthOK);
        inputEvents.add(e);
        inputEvents.add(eSkip);
        inputEvents.add(eAuth);
        
        Collection<Event> results = filter.filter(inputEvents, matchListener);

        Assert.assertEquals(2, results.size());
        Assert.assertEquals(true, e.getField("tags") == null );
        Assert.assertEquals(true, eAuth.getField("tags") == null);
        Assert.assertEquals(2, matchListener.getMatchCount());
    }

/**
     * Tests that localhost IPs stay that way, if no "server_ip" field exist in logstash event.
     *
     * NOTE: Events with "(NONE)" have no users[], so they are removed from events.
     */
    @Test
    public void testParseMongoSyslog_localhostIPs_remain() {
        final String mongodString = "<14>Feb 18 08:53:31 qa-db51 mongod: { \"atype\" : \"authCheck\", \"ts\" : { \"$date\" : \"2020-01-16T05:41:30.783-0500\" }, \"local\" : { \"ip\" : \"127.0.0.1\", \"port\" : 10400 }, \"remote\" : { \"ip\" : \"127.0.0.1\", \"port\" : 27017 }, \"users\" : [ { \"user\" : \"realAdmin\", \"db\" : \"admin\" } ], \"roles\" : [ { \"role\" : \"readWriteAnyDatabase\", \"db\" : \"admin\" }, { \"role\" : \"userAdminAnyDatabase\", \"db\" : \"admin\" } ], \"param\" : { \"command\" : \"find\", \"ns\" : \"config.transactions\", \"args\" : { \"find\" : \"transactions\", \"filter\" : { \"lastWriteDate\" : { \"$lt\" : { \"$date\" : \"2020-01-16T05:11:30.782-0500\" } } }, \"projection\" : { \"_id\" : 1 }, \"sort\" : { \"_id\" : 1 }, \"$db\" : \"config\" } }, \"result\" : 0 }";
        final String expectedIP = "127.0.0.1";

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();
        
        e.setField("message", mongodString); // with "(NONE)" local/remote IPs
        // no server_ip field
        
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        String recordString = e.getField("Record").toString();
        Record record = (new Gson()).fromJson(recordString, Record.class);
        Assert.assertNotNull(record);

        Assert.assertEquals(
                "When no server_ip field, MongoDB remote IP should not be overriden.",
                expectedIP, record.getSessionLocator().getClientIp());
        Assert.assertEquals(
                "When no server_ip field, MongoDB local IP should not be overriden.",
                expectedIP, record.getSessionLocator().getServerIp());
    }
    /**
     * Tests that localhost IPs are overriden with "server_ip", if exists in logstash Event.
     
     * On events with local IP, convert to server IP, if sent thru logstash event by Filebeat/syslog. 
     * NOTE: Events with "(NONE)" have no users[], so they are removed from events.
     */
    @Test
    public void testParseMongoSyslog_localhostIP_override() {
        final String mongodString = "<14>Feb 18 08:53:31 qa-db51 mongod: { \"atype\" : \"authCheck\", \"ts\" : { \"$date\" : \"2020-01-16T05:41:30.783-0500\" }, \"local\" : { \"ip\" : \"127.0.0.1\", \"port\" : 10400 }, \"remote\" : { \"ip\" : \"127.0.0.1\", \"port\" : 27017 }, \"users\" : [ { \"user\" : \"realAdmin\", \"db\" : \"admin\" } ], \"roles\" : [ { \"role\" : \"readWriteAnyDatabase\", \"db\" : \"admin\" }, { \"role\" : \"userAdminAnyDatabase\", \"db\" : \"admin\" } ], \"param\" : { \"command\" : \"find\", \"ns\" : \"config.transactions\", \"args\" : { \"find\" : \"transactions\", \"filter\" : { \"lastWriteDate\" : { \"$lt\" : { \"$date\" : \"2020-01-16T05:11:30.782-0500\" } } }, \"projection\" : { \"_id\" : 1 }, \"sort\" : { \"_id\" : 1 }, \"$db\" : \"config\" } }, \"result\" : 0 }";
        final String expectedIP = "20.30.40.50";

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();
        
        e.setField("message", mongodString); // with "(NONE)" local/remote IPs
        e.setField("server_ip", expectedIP);
        
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        String recordString = e.getField("Record").toString();
        Record record = (new Gson()).fromJson(recordString, Record.class);
        Assert.assertNotNull(record);

        Assert.assertEquals(
                "server_ip field should be used, if exists, when MongoDB remote IP points to localhost.",
                expectedIP, record.getSessionLocator().getClientIp());
        Assert.assertEquals(
                "server_ip field should be used, if exists, when MongoDB local IP points to localhost.",
                expectedIP, record.getSessionLocator().getServerIp());
    }

    @Test
    public void testParseMongoSyslog_doNotInjectHost() {
        // syslog message uses different IPs for local/remote:
        final String mongodString = "<14>Feb 18 08:53:31 qa-db51 mongod: { \"atype\" : \"authCheck\", \"ts\" : { \"$date\" : \"2020-01-16T05:41:30.783-0500\" }, \"local\" : { \"ip\" : \"1.2.3.456\", \"port\" : 0 }, \"remote\" : { \"ip\" : \"1.2.3.123\", \"port\" : 0 }, \"users\" : [ { \"user\" : \"realAdmin\", \"db\" : \"admin\" } ], \"roles\" : [ { \"role\" : \"readWriteAnyDatabase\", \"db\" : \"admin\" }, { \"role\" : \"userAdminAnyDatabase\", \"db\" : \"admin\" } ], \"roles\" : [], \"param\" : { \"command\" : \"find\", \"ns\" : \"config.transactions\", \"args\" : { \"find\" : \"transactions\", \"filter\" : { \"lastWriteDate\" : { \"$lt\" : { \"$date\" : \"2020-01-16T05:11:30.782-0500\" } } }, \"projection\" : { \"_id\" : 1 }, \"sort\" : { \"_id\" : 1 }, \"$db\" : \"config\" } }, \"result\" : 0 }";
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


    /**
     * Simulates cut-off towards end of Syslog message, and tests proper JSON parse error tag is attached 
     * */
    @Test 
    public void testTagParseError() {
        String messageString = "<14>Feb 18 08:53:31 qa-db51 mongod: { \"atype\" : \"authCheck\", \"ts\" : { \"$date\" : \"2020-06-11T09:44:11.070-0400\" }, \"local\" : { \"ip\" : \"9.70.147.59\", \"port\" : 27017 }, \"remote\" : { \"ip\" : \"9.148.202.94\", \"port\" : 60185 }, \"users\" : [ { \"user\" : \"realAdmin\", \"db\" : \"admin\" } ], \"roles\" : [ { \"role\" : \"readWriteAnyDatabase\", \"db\" : \"admin\" }, { \"role\" : \"userAdminAnyDatabase\", \"db\" : \"admin\" } ], \"param\" : { \"command\" : \"find\", \"ns\" : \"admin.USERS\", \"args\" : { \"find\" : \"USERS\", \"filter\" : {}, \"lsid\" : { \"id\" : { \"$binary\" : \"mV20eHvvRha2ELTeqJxQJg==\", \"$type\" : \"04\" } }, \"$db\" : \"admin\", \"$readPreference\" : { \"mode\" : \"primaryPreferred\" } } }, \"res"; 
        Context context = new ContextImpl(null, null);
        JavaFilterExample filter = new JavaFilterExample("test-id", null, context);

        Event e = new org.logstash.Event();
        TestMatchListener matchListener = new TestMatchListener();
        
        e.setField("message", messageString);
        Collection<Event> results = filter.filter(Collections.singletonList(e), matchListener);

        Assert.assertEquals(1, results.size());
        Assert.assertEquals(true, e.getField("tags").toString().contains(
            JavaFilterExample.LOGSTASH_TAG_JSON_PARSE_ERROR));
        Assert.assertEquals(0, matchListener.getMatchCount()); // just sigals as not a match, so no further tags will be added, in pipeline.
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