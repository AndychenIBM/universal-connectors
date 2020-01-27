package com.ibm.guardium;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Assert;
import org.junit.Test;

public class ParserTest {

    @Test
    public void testParseAsConstruct_Find() {
        final String mongoString = "{ \"atype\": \"authCheck\", \"ts\": { \"$date\": \"2020-01-14T10:46:02.431-0500\" }, \"local\": { \"ip\": \"127.0.0.1\", \"port\": 27017 }, \"remote\": { \"ip\": \"127.0.0.1\", \"port\": 33708 }, \"users\": [], \"roles\": [], \"param\": { \"command\": \"find\", \"ns\": \"test.bios\", \"args\": { \"find\": \"bios\", \"filter\": {}, \"lsid\": { \"id\": { \"$binary\": \"hg6ugx4ASiGWKSPiDRlEFw==\", \"$type\": \"04\" } }, \"$db\": \"test\" } }, \"result\": 0 }";
        
        final JsonObject mongoJson = JsonParser.parseString(mongoString).getAsJsonObject();
        //final String actualResult = Parser.Parse(mongoJson);
        final Construct result = Parser.ParseAsConstruct(mongoJson);

        final Sentence sentence = result.sentences.get(0);
        Assert.assertEquals("find", sentence.verb);
        Assert.assertEquals("bios", sentence.objects.get(0).name);
        Assert.assertEquals("collection", sentence.objects.get(0).type);
    }

    @Test
    public void testParseAsConstruct_insert() {
        final String mongoString = "{ \"atype\": \"authCheck\", \"ts\": { \"$date\": \"2020-01-21T04:37:30.174-0500\" }, \"local\": { \"ip\": \"127.0.0.1\", \"port\": 27017 }, \"remote\": { \"ip\": \"127.0.0.1\", \"port\": 47638 }, \"users\": [ { \"user\": \"BILL\", \"db\": \"admin\" } ], \"roles\": [ { \"role\": \"readWrite\", \"db\": \"admin\" } ], \"param\": { \"command\": \"insert\", \"ns\": \"test.Myuser\", \"args\": { \"insert\": \"Myuser\", \"ordered\": true, \"lsid\": { \"id\": { \"$binary\": \"ql5vZfbGTgWXrBSZOU6l5w==\", \"$type\": \"04\" } }, \"$db\": \"test\", \"documents\": [ { \"_id\": { \"$oid\": \"58842568c706f50f5c1de663\" }, \"userId\": \"123456\", \"user_name\": \"Eli\", \"interestedTags\": [ \"music\", \"cricket\", \"hiking\", \"F1\", \"Mobile\", \"racing\" ], \"listFriends\": [ \"111111\", \"222222\", \"333333\" ] } ] } }, \"result\": 0 }";
        final JsonObject mongoJson = JsonParser.parseString(mongoString).getAsJsonObject();
        final Construct result = Parser.ParseAsConstruct(mongoJson);

        final Sentence sentence = result.sentences.get(0);
        Assert.assertEquals("insert", sentence.verb);
        Assert.assertEquals("Myuser", sentence.objects.get(0).name);
    }

    @Test
    public void testParseAsConstruct_deleteOne() {
        final String mongoString = "{ 'atype': 'authCheck', 'ts': { '$date': '2020-01-26T08:25:10.527-0500' }, 'local': { 'ip': '127.0.0.1', 'port': 27017 }, 'remote': { 'ip': '127.0.0.1', 'port': 56470 }, 'users': [], 'roles': [], 'param': { 'command': 'delete', 'ns': 'test.posts', 'args': { 'delete': 'posts', 'ordered': true, 'lsid': { 'id': { '$binary': '1P3A98W7QbqeDMqMdP2trA==', '$type': '04' } }, '$db': 'test', 'deletes': [ { 'q': { 'owner_id': '12345' }, 'limit': 1 } ] } }, 'result': 0 }";
        final JsonObject mongoJson = JsonParser.parseString(mongoString).getAsJsonObject();
        final Construct result = Parser.ParseAsConstruct(mongoJson);

        final Sentence sentence = result.sentences.get(0);
        Assert.assertEquals("delete", sentence.verb);
        Assert.assertEquals("posts", sentence.objects.get(0).name);
    }
    
    @Test 
    public void testParseAsConstruct_updateOne() {
        final String mongoString = "{ 'atype': 'authCheck', 'ts': { '$date': '2020-01-26T08:57:50.972-0500' }, 'local': { 'ip': '127.0.0.1', 'port': 27017 }, 'remote': { 'ip': '127.0.0.1', 'port': 56470 }, 'users': [], 'roles': [], 'param': { 'command': 'update', 'ns': 'test.posts', 'args': { 'update': 'posts', 'ordered': true, 'lsid': { 'id': { '$binary': '1P3A98W7QbqeDMqMdP2trA==', '$type': '04' } }, '$db': 'test', 'updates': [ { 'q': { 'owner_id': '6789' }, 'u': { '$set': { 'via': 'instagram' } }, 'multi': false, 'upsert': false } ] } }, 'result': 0 }";
        final JsonObject mongoJson = JsonParser.parseString(mongoString).getAsJsonObject();
        final Construct result = Parser.ParseAsConstruct(mongoJson);

        final Sentence sentence = result.sentences.get(0);
        Assert.assertEquals("update", sentence.verb);
        Assert.assertEquals("posts", sentence.objects.get(0).name);
        
    }
    @Test
    public void testParseAsConstruct_Aggregate() {
        final String mongoString = "{ \"atype\": \"authCheck\", \"ts\": { \"$date\": \"2020-01-16T06:07:21.122-0500\" }, \"local\": { \"ip\": \"127.0.0.1\", \"port\": 27017 }, \"remote\": { \"ip\": \"127.0.0.1\", \"port\": 43600 }, \"users\": [], \"roles\": [], \"param\": { \"command\": \"aggregate\", \"ns\": \"test.users\", \"args\": { \"aggregate\": \"users\", \"pipeline\": [ { \"$match\": { \"admin\": 1 } }, { \"$lookup\": { \"from\": \"posts\", \"localField\": \"_id\", \"foreignField\": \"owner_id\", \"as\": \"posts\" } }, { \"$project\": { \"posts\": { \"$filter\": { \"input\": \"$posts\", \"as\": \"post\", \"cond\": { \"$eq\": [ \"$$post.via\", \"facebook\" ] } } }, \"admin\": 1 } } ], \"cursor\": {}, \"lsid\": { \"id\": { \"$binary\": \"9IxV+mBmQfa73jbV9n4CSQ==\", \"$type\": \"04\" } }, \"$db\": \"test\" } }, \"result\": 0 }";
        
        final JsonObject mongoJson = JsonParser.parseString(mongoString).getAsJsonObject();
        //final String actualResult = Parser.Parse(mongoJson);
        final Construct result = Parser.ParseAsConstruct(mongoJson);

        final Sentence sentence = result.sentences.get(0);
        Assert.assertEquals("aggregate", sentence.verb);
        Assert.assertEquals("users", sentence.objects.get(0).name);
        Assert.assertEquals("posts", sentence.objects.get(1).name);
    }

    @Test
    public void testParseAsConstruct_Aggregate_nLookups() {
        // TODO
        /* final String mongoString = "{ \"atype\": \"authCheck\", \"ts\": { \"$date\": \"2020-01-16T06:07:21.122-0500\" }, \"local\": { \"ip\": \"127.0.0.1\", \"port\": 27017 }, \"remote\": { \"ip\": \"127.0.0.1\", \"port\": 43600 }, \"users\": [], \"roles\": [], \"param\": { \"command\": \"aggregate\", \"ns\": \"test.users\", \"args\": { \"aggregate\": \"users\", \"pipeline\": [ { \"$match\": { \"admin\": 1 } }, { \"$lookup\": { \"from\": \"posts\", \"localField\": \"_id\", \"foreignField\": \"owner_id\", \"as\": \"posts\" } }, { \"$project\": { \"posts\": { \"$filter\": { \"input\": \"$posts\", \"as\": \"post\", \"cond\": { \"$eq\": [ \"$$post.via\", \"facebook\" ] } } }, \"admin\": 1 } } ], \"cursor\": {}, \"lsid\": { \"id\": { \"$binary\": \"9IxV+mBmQfa73jbV9n4CSQ==\", \"$type\": \"04\" } }, \"$db\": \"test\" } }, \"result\": 0 }";
        
        final JsonObject mongoJson = JsonParser.parseString(mongoString).getAsJsonObject();
        //final String actualResult = Parser.Parse(mongoJson);
        Construct result = Parser.ParseAsConstruct(mongoJson);

        Sentence sentence = result.sentences.get(0);
        Assert.assertEquals("aggregate", sentence.verb);
        Assert.assertEquals("users", sentence.objects.get(0).name);
        Assert.assertEquals("posts", sentence.objects.get(1).name); */
    }

    @Test
    public void testParseAsConstruct_Aggregate_graphLookup() {
        final String mongoString = "{ 'atype': 'authCheck', 'ts': { '$date': '2020-01-26T09:58:44.547-0500' }, 'local': { 'ip': '127.0.0.1', 'port': 27017 }, 'remote': { 'ip': '127.0.0.1', 'port': 56984 }, 'users': [], 'roles': [], 'param': { 'command': 'aggregate', 'ns': 'test.travelers', 'args': { 'aggregate': 'travelers', 'pipeline': [ { '$graphLookup': { 'from': 'airports', 'startWith': '$nearestAirport', 'connectFromField': 'connects', 'connectToField': 'airport', 'maxDepth': 2, 'depthField': 'numConnections', 'as': 'destinations' } } ], 'cursor': {}, 'lsid': { 'id': { '$binary': '2WoIDPhSTcKHrdJW4azoow==', '$type': '04' } }, '$db': 'test' } }, 'result': 0 }";
        
        final JsonObject mongoJson = JsonParser.parseString(mongoString).getAsJsonObject();
        //final String actualResult = Parser.Parse(mongoJson);
        final Construct result = Parser.ParseAsConstruct(mongoJson);

        final Sentence sentence = result.sentences.get(0);
        Assert.assertEquals("aggregate", sentence.verb);
        Assert.assertEquals("travelers", sentence.objects.get(0).name);
        Assert.assertEquals("airports", sentence.objects.get(1).name);
    }
    
}