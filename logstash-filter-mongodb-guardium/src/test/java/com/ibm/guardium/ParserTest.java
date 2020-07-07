package com.ibm.guardium;

import static org.junit.Assert.assertEquals;

import java.sql.Date;
import java.text.ParseException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.guardium.connector.structures.Accessor;
import com.ibm.guardium.connector.structures.Construct;
import com.ibm.guardium.connector.structures.Data;
import com.ibm.guardium.connector.structures.ExceptionRecord;
import com.ibm.guardium.connector.structures.Record;
import com.ibm.guardium.connector.structures.Sentence;
import com.ibm.guardium.connector.structures.SessionLocator;

import org.junit.Assert;
import org.junit.Test;

public class ParserTest {

    Parser parser = new Parser();
    final String mongoString = "{ \"atype\": \"authCheck\", \"ts\": { \"$date\": \"2020-01-26T09:58:44.547-0500\" }, \"local\": { \"ip\": \"127.0.0.1\", \"port\": 27017 }, \"remote\": { \"ip\": \"127.0.0.1\", \"port\": 56984 }, \"users\": [], \"roles\": [], \"param\": { \"command\": \"aggregate\", \"ns\": \"test.travelers\", \"args\": { \"aggregate\": \"travelers\", \"pipeline\": [ { \"$graphLookup\": { \"from\": \"airports\", \"startWith\": \"$nearestAirport\", \"connectFromField\": \"connects\", \"connectToField\": \"airport\", \"maxDepth\": 2, \"depthField\": \"numConnections\", \"as\": \"destinations\" } } ], \"cursor\": {}, \"lsid\": { \"id\": { \"$binary\": \"2WoIDPhSTcKHrdJW4azoow==\", \"$type\": \"04\" } }, \"$db\": \"test\" } }, \"result\": 0 }";
    final JsonObject mongoJson = JsonParser.parseString(mongoString).getAsJsonObject();

    @Test
    public void testParseAsConstruct_Find() {
        final String mongoString = "{ \"atype\": \"authCheck\", \"ts\": { \"$date\": \"2020-01-14T10:46:02.431-0500\" }, \"local\": { \"ip\": \"127.0.0.1\", \"port\": 27017 }, \"remote\": { \"ip\": \"127.0.0.1\", \"port\": 33708 }, \"users\": [], \"roles\": [], \"param\": { \"command\": \"find\", \"ns\": \"test.bios\", \"args\": { \"find\": \"bios\", \"filter\": {}, \"lsid\": { \"id\": { \"$binary\": \"hg6ugx4ASiGWKSPiDRlEFw==\", \"$type\": \"04\" } }, \"$db\": \"test\" } }, \"result\": 0 }";

        final JsonObject mongoJson = JsonParser.parseString(mongoString).getAsJsonObject();
        // final String actualResult = Parser.Parse(mongoJson);
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
        // final String actualResult = Parser.Parse(mongoJson);
        final Construct result = Parser.ParseAsConstruct(mongoJson);

        final Sentence sentence = result.sentences.get(0);
        Assert.assertEquals("aggregate", sentence.verb);
        Assert.assertEquals("users", sentence.objects.get(0).name);
        Assert.assertEquals("posts", sentence.objects.get(1).name);
    }

    @Test
    public void testParseAsConstruct_Aggregate_nLookups() {
        final String mongoString = "{ \"atype\": \"authCheck\", \"ts\": { \"$date\": \"2020-01-16T06:07:21.122-0500\" }, \"local\": { \"ip\": \"127.0.0.1\", \"port\": 27017 }, \"remote\": { \"ip\": \"127.0.0.1\", \"port\": 43600 }, \"users\": [], \"roles\": [], \"param\": { \"command\": \"aggregate\", \"ns\": \"test.users\", \"args\": { \"aggregate\": \"users\", \"pipeline\": [ { \"$match\": { \"admin\": 1 } }, { \"$lookup\": { \"from\": \"posts\", \"localField\": \"_id\", \"foreignField\": \"owner_id\", \"as\": \"posts\" } }, { \"$project\": { \"posts\": { \"$filter\": { \"input\": \"$posts\", \"as\": \"post\", \"cond\": { \"$eq\": [ \"$$post.via\", \"facebook\" ] } } }, \"admin\": 1 } } ], \"cursor\": {}, \"lsid\": { \"id\": { \"$binary\": \"9IxV+mBmQfa73jbV9n4CSQ==\", \"$type\": \"04\" } }, \"$db\": \"test\" } }, \"result\": 0 }";

        final JsonObject mongoJson = JsonParser.parseString(mongoString).getAsJsonObject();
        // final String actualResult = Parser.Parse(mongoJson);
        final Construct result = Parser.ParseAsConstruct(mongoJson);

        final Sentence sentence = result.sentences.get(0);
        Assert.assertEquals("aggregate", sentence.verb);
        Assert.assertEquals("users", sentence.objects.get(0).name);
        Assert.assertEquals("posts", sentence.objects.get(1).name);
    }

    @Test
    public void testParseAsConstruct_Aggregate_graphLookup() {
        final String mongoString = "{ \"atype\": \"authCheck\", \"ts\": { \"$date\": \"2020-01-16T06:07:21.122-0500\" }, \"local\": { \"ip\": \"127.0.0.1\", \"port\": 27017 }, \"remote\": { \"ip\": \"127.0.0.1\", \"port\": 43600 }, \"users\": [], \"roles\": [], \"param\": { \"command\": \"aggregate\", \"ns\": \"test.users\", \"args\": { \"aggregate\": \"users\", \"pipeline\": [ { \"$match\": { \"admin\": 1 } }, { \"$lookup\": { \"from\": \"posts\", \"localField\": \"_id\", \"foreignField\": \"owner_id\", \"as\": \"posts\" } }, { \"$lookup\": { \"from\": \"packages\", \"localField\": \"_id\", \"foreignField\": \"owner_id\", \"as\": \"packagePosts\" } }, { \"$project\": { \"posts\": { \"$filter\": { \"input\": \"$posts\", \"as\": \"post\", \"cond\": { \"$eq\": [ \"$$post.via\", \"facebook\" ] } } }, \"admin\": 1 } } ], \"cursor\": {}, \"lsid\": { \"id\": { \"$binary\": \"9IxV+mBmQfa73jbV9n4CSQ==\", \"$type\": \"04\" } }, \"$db\": \"test\" } }, \"result\": 0 }";

        final JsonObject mongoJson = JsonParser.parseString(mongoString).getAsJsonObject();
        // final String actualResult = Parser.Parse(mongoJson);
        final Construct result = Parser.ParseAsConstruct(mongoJson);

        final Sentence sentence = result.sentences.get(0);
        Assert.assertEquals("aggregate", sentence.verb);
        Assert.assertEquals("users", sentence.objects.get(0).name);
        Assert.assertEquals("posts", sentence.objects.get(1).name); // 1st lookup
        Assert.assertEquals("packages", sentence.objects.get(2).name); // 2nd lookup
    }

    @Test 
    public void testParseRecord_createIndex() throws ParseException {
        final String mongoString = "{ \"atype\" : \"authCheck\", \"ts\" : { \"$date\" : \"2020-06-03T07:48:55.762-0400\" }, \"local\" : { \"ip\" : \"127.0.0.1\", \"port\" : 27017 }, \"remote\" : { \"ip\" : \"127.0.0.1\", \"port\" : 41112 }, \"users\" : [ { \"user\" : \"realAdmin\", \"db\" : \"admin\" } ], \"roles\" : [ { \"role\" : \"readWriteAnyDatabase\", \"db\" : \"admin\" }, { \"role\" : \"userAdminAnyDatabase\", \"db\" : \"admin\" } ], \"param\" : { \"command\" : \"createIndexes\", \"ns\" : \"newDB01.newCollecti\", \"args\" : { \"createIndexes\" : \"newCollecti\", \"indexes\" : [ { \"key\" : { \"category\" : 1 }, \"name\" : \"testIDX1\" } ], \"lsid\" : { \"id\" : { \"$binary\" : \"T52tf2+yRbu31sZ5kq9R7Q==\", \"$type\" : \"04\" } }, \"$db\" : \"newDB01\" } }, \"result\" : 0 }";
        final JsonObject mongoJson = JsonParser.parseString(mongoString).getAsJsonObject();
        Record record = Parser.parseRecord(mongoJson);
        final Construct construct = record.getData().getConstruct();
        final Sentence sentence = construct.sentences.get(0);
        
        Assert.assertEquals("createIndexes", sentence.verb);
        Assert.assertEquals("newCollecti", sentence.objects.get(0).name);
        Assert.assertEquals("newDB01", record.getDbName());
    }

    @Test
    public void testParseRecord_createCollection() throws ParseException {
        // skipping dedicated log messages "{ \"atype\" : \"createCollection\", \"ts\" : { \"$date\" : \"2020-05-11T06:24:38.168-0400\" } , \"local\" : { \"ip\" : \"127.0.0.1\", \"port\" : 27017 } , \"remote\" : { \"ip\" : \"127.0.0.1\", \"port\" : 48458 } , \"users\" : [], \"roles\" : [], \"param\" : { \"ns\" : \"test.collection3\" } , \"result\" : 0 }";
        final String mongoString = "{ \"atype\" : \"authCheck\", \"ts\" : { \"$date\" : \"2020-06-03T03:40:30.888-0400\" }, \"local\" : { \"ip\" : \"127.0.0.1\", \"port\" : 27017 }, \"remote\" : { \"ip\" : \"127.0.0.1\", \"port\" : 40426 }, \"users\" : [ { \"user\" : \"realAdmin\", \"db\" : \"admin\" } ], \"roles\" : [ { \"role\" : \"readWriteAnyDatabase\", \"db\" : \"admin\" }, { \"role\" : \"userAdminAnyDatabase\", \"db\" : \"admin\" } ], \"param\" : { \"command\" : \"create\", \"ns\" : \"newDB01.newCollection01\", \"args\" : { \"create\" : \"newCollection01\", \"lsid\" : { \"id\" : { \"$binary\" : \"CsTWBZwaQnOweCDDbiJWng==\", \"$type\" : \"04\" } }, \"$db\" : \"newDB01\" } }, \"result\" : 0 }";
        final JsonObject mongoJson = JsonParser.parseString(mongoString).getAsJsonObject();
        Record record = Parser.parseRecord(mongoJson);
        final Construct construct = record.getData().getConstruct();
        final Sentence sentence = construct.sentences.get(0);
        
        Assert.assertEquals("create", sentence.verb);
        Assert.assertEquals("newCollection01", sentence.objects.get(0).name);
        Assert.assertEquals("newDB01", record.getDbName());
    }

    /**
     * NOT USED: Test authorization error messsage can be parsed as usual (query is needed later)
     */
    // @Test 
    // public void testParseAsConstruct_AuthorizationError() {
    //     final String mongoString = "{ \"atype\" : \"authCheck\", \"ts\" : { \"$date\" : \"2020-05-17T11:29:02.773-0400\" }, \"local\" : { \"ip\" : \"127.0.0.1\", \"port\" : 27017 }, \"remote\" : { \"ip\" : \"127.0.0.1\", \"port\" : 29360 }, \"users\" : [ { \"user\" : \"readerUser\", \"db\" : \"admin\" } ], \"roles\" : [ { \"role\" : \"read\", \"db\" : \"admin\" } ], \"param\" : { \"command\" : \"insert\", \"ns\" : \"admin.USERS\", \"args\" : { \"insert\" : \"USERS\", \"ordered\" : true, \"lsid\" : { \"id\" : { \"$binary\" : \"EQSjmxPcSxyNN6Vw7Wy1pQ==\", \"$type\" : \"04\" } }, \"$db\" : \"admin\", \"documents\" : [ { \"_id\" : { \"$oid\" : \"5ec1583e3a55d1ed961be47e\" }, \"uid\" : 2, \"name\" : \"Tal\" } ] } }, \"result\" : 13 }";
    //     final JsonObject mongoJson = JsonParser.parseString(mongoString).getAsJsonObject();
    //     // final String actualResult = Parser.Parse(mongoJson);
    //     final Construct result = Parser.ParseAsConstruct(mongoJson);

    //     final Sentence sentence = result.sentences.get(0);
    //     Assert.assertEquals("insert", sentence.verb);
    //     Assert.assertEquals("USERS", sentence.objects.get(0).name);
    // }
    @Test 
    public void testParseRecord_AuthorizationError() throws ParseException {
        final String mongoString = "{ \"atype\" : \"authCheck\", \"ts\" : { \"$date\" : \"2020-05-17T11:29:02.773-0400\" }, \"local\" : { \"ip\" : \"127.0.0.1\", \"port\" : 27017 }, \"remote\" : { \"ip\" : \"127.0.0.1\", \"port\" : 29360 }, \"users\" : [ { \"user\" : \"readerUser\", \"db\" : \"admin\" } ], \"roles\" : [ { \"role\" : \"read\", \"db\" : \"admin\" } ], \"param\" : { \"command\" : \"insert\", \"ns\" : \"admin.USERS\", \"args\" : { \"insert\" : \"USERS\", \"ordered\" : true, \"lsid\" : { \"id\" : { \"$binary\" : \"EQSjmxPcSxyNN6Vw7Wy1pQ==\", \"$type\" : \"04\" } }, \"$db\" : \"admin\", \"documents\" : [ { \"_id\" : { \"$oid\" : \"5ec1583e3a55d1ed961be47e\" }, \"uid\" : 2, \"name\" : \"Tal\" } ] } }, \"result\" : 13 }";
        final JsonObject mongoJson = JsonParser.parseString(mongoString).getAsJsonObject();
        
        final Record record = Parser.parseRecord(mongoJson);
        
        Assert.assertEquals(
            Parser.EXCEPTION_TYPE_AUTHORIZATION_STRING,
            record.getException().getExceptionTypeId()
            );

        Assert.assertEquals(mongoString.replace(" ", ""), record.getException().getSqlString());
        Assert.assertEquals("readerUser", record.getAccessor().getDbUser().trim());
        Assert.assertEquals(Parser.SERVER_TYPE_STRING, record.getAccessor().getServerType());
    }


    @Test
    public void testParseRecord_AuthenticationError() throws ParseException {
        final String mongoString = "{ \"atype\" : \"authenticate\", \"ts\" : { \"$date\" : \"2020-05-17T11:37:30.421-0400\" }, \"local\" : { \"ip\" : \"127.0.0.1\", \"port\" : 27017 }, \"remote\" : { \"ip\" : \"127.0.0.1\", \"port\" : 29398 }, \"users\" : [], \"roles\" : [], \"param\" : { \"user\" : \"readerUser\", \"db\" : \"admin\", \"mechanism\" : \"SCRAM-SHA-256\" }, \"result\" : 18 }";
        final JsonObject mongoJson = JsonParser.parseString(mongoString).getAsJsonObject();
        // final String actualResult = Parser.Parse(mongoJson);
        final Record record = Parser.parseRecord(mongoJson);

        Assert.assertEquals("readerUser", record.getAccessor().getDbUser().trim());
        
        ExceptionRecord exceptionRecord = record.getException();
        Assert.assertEquals(
            "exception type id should be known to Guardium and match error", 
            Parser.EXCEPTION_TYPE_AUTHENTICATION_STRING, 
            exceptionRecord.getExceptionTypeId());
        Assert.assertEquals(mongoString.replace(" ", ""), exceptionRecord.getSqlString());
    }

    @Test 
    public void testLogFile() {
        // TODO: read from file stream
        final String mongoString = "{ \"atype\" : \"authCheck\", \"ts\" : { \"$date\" : \"2020-02-16T03:36:53.800-0500\" }, \"local\" : { \"ip\" : \"(NONE)\", \"port\" : 0 }, \"remote\" : { \"ip\" : \"(NONE)\", \"port\" : 0 }, \"users\" : [], \"roles\" : [], \"param\" : { \"command\" : \"listIndexes\", \"ns\" : \"config.system.sessions\", \"args\" : { \"listIndexes\" : \"system.sessions\", \"cursor\" : {}, \"$db\" : \"config\" } }, \"result\" : 0 }";

        final JsonObject mongoJson = JsonParser.parseString(mongoString).getAsJsonObject();
        // final String actualResult = Parser.Parse(mongoJson);
        final Construct result = Parser.ParseAsConstruct(mongoJson);

        final Sentence sentence = result.sentences.get(0);
        Assert.assertEquals("listIndexes", sentence.verb);
        Assert.assertEquals("system.sessions", sentence.objects.get(0).name);
    }

    @Test 
    public void testRedactData() {
        final String mongoString = "{ \"atype\": \"authCheck\", \"ts\": { \"$date\": \"2020-01-21T04:37:30.174-0500\" }, \"local\": { \"ip\": \"127.0.0.1\", \"port\": 27017 }, \"remote\": { \"ip\": \"127.0.0.1\", \"port\": 47638 }, \"users\": [ { \"user\": \"BILL\", \"db\": \"admin\" } ], \"roles\": [ { \"role\": \"readWrite\", \"db\": \"admin\" } ], \"param\": { \"command\": \"insert\", \"ns\": \"test.Myuser\", \"args\": { \"insert\": \"Myuser\", \"ordered\": true, \"lsid\": { \"id\": { \"$binary\": \"ql5vZfbGTgWXrBSZOU6l5w==\", \"$type\": \"04\" } }, \"$db\": \"test\", \"documents\": [ { \"_id\": { \"$oid\": \"58842568c706f50f5c1de663\" }, \"userId\": \"123456\", \"user_name\": \"Eli\", \"interestedTags\": [ \"music\", \"cricket\", \"hiking\", \"F1\", \"Mobile\", \"racing\" ], \"listFriends\": [ \"111111\", \"222222\", \"333333\" ] } ] } }, \"result\": 0 }";
        final String mongoRedactedString = "{ \"atype\": \"authCheck\", \"ts\": { \"$date\": \"2020-01-21T04:37:30.174-0500\" }, \"local\": { \"ip\": \"127.0.0.1\", \"port\": 27017 }, \"remote\": { \"ip\": \"127.0.0.1\", \"port\": 47638 }, \"users\": [ { \"user\": \"BILL\", \"db\": \"admin\" } ], \"roles\": [ { \"role\": \"readWrite\", \"db\": \"admin\" } ], \"param\": { \"command\": \"insert\", \"ns\": \"test.Myuser\", \"args\": { \"insert\": \"Myuser\", \"ordered\": \"?\", \"lsid\": { \"id\": { \"$binary\": \"?\", \"$type\": \"?\" } }, \"$db\": \"test\", \"documents\": [ { \"_id\": { \"$oid\": \"?\" }, \"userId\": \"?\", \"user_name\": \"?\", \"interestedTags\": [ \"?\", \"?\", \"?\", \"?\", \"?\", \"?\" ], \"listFriends\": [ \"?\", \"?\", \"?\" ] } ] } }, \"result\": 0 }";
        final JsonObject mongoRedactedJsonObject = JsonParser.parseString(mongoRedactedString).getAsJsonObject();
        final JsonObject mongoJson = JsonParser.parseString(mongoString).getAsJsonObject();
        final JsonObject args = mongoJson.get("param").getAsJsonObject().get("args").getAsJsonObject();
        
        Parser.RedactWithExceptions(mongoJson); // overrides args in-place
        
        final JsonArray array6redacted = new JsonArray(6);
        for (int i=0; i<6; i++) {
            array6redacted.add("?"); 
        }

        Assert.assertEquals("Collection name should not be redacted", args.get("$db").getAsString(), "test");
        Assert.assertEquals("Object should not be redacted", args.get("insert").getAsString(), "Myuser"); 
        Assert.assertEquals(args.get("documents").getAsJsonArray()
            .get(0).getAsJsonObject().get("interestedTags").getAsJsonArray(), array6redacted);
        Assert.assertEquals(mongoRedactedJsonObject, mongoJson);
    }

    /** 
     * Tests that sensitive data is redacted. 
     * Tests also that MongoDB $lookup and $graphlookup required arguments should not be redacted, 
     * as well "as" value is not redacted, as it sets a field name to be used in result. 
     */
    @Test 
    public void testRedactData_aggregate() {
        final String mongoString = "{ \"atype\": \"authCheck\", \"ts\": { \"$date\": \"2020-01-16T06:07:21.122-0500\" }, \"local\": { \"ip\": \"127.0.0.1\", \"port\": 27017 }, \"remote\": { \"ip\": \"127.0.0.1\", \"port\": 43600 }, \"users\": [], \"roles\": [], \"param\": { \"command\": \"aggregate\", \"ns\": \"test.users\", \"args\": { \"aggregate\": \"users\", \"pipeline\": [ { \"$match\": { \"admin\": 1 } }, { \"$lookup\": { \"from\": \"posts\", \"localField\": \"_id\", \"foreignField\": \"owner_id\", \"as\": \"posts\", \"let\": { \"foo\": \"5\"} } }, { \"$project\": { \"posts\": { \"$filter\": { \"input\": \"$posts\", \"as\": \"post\", \"cond\": { \"$eq\": [ \"$$post.via\", \"facebook\" ] } } }, \"admin\": 1 } } ], \"cursor\": {}, \"lsid\": { \"id\": { \"$binary\": \"9IxV+mBmQfa73jbV9n4CSQ==\", \"$type\": \"04\" } }, \"$db\": \"test\" } }, \"result\": 0 }";
        final String mongoRedactedString = "{ \"atype\": \"authCheck\", \"ts\": { \"$date\": \"2020-01-16T06:07:21.122-0500\" }, \"local\": { \"ip\": \"127.0.0.1\", \"port\": 27017 }, \"remote\": { \"ip\": \"127.0.0.1\", \"port\": 43600 }, \"users\": [], \"roles\": [], \"param\": { \"command\": \"aggregate\", \"ns\": \"test.users\", \"args\": { \"aggregate\": \"users\", \"pipeline\": [ { \"$match\": { \"admin\": \"?\" } }, { \"$lookup\": { \"from\": \"posts\", \"localField\": \"_id\", \"foreignField\": \"owner_id\", \"as\": \"posts\", \"let\": { \"foo\": \"?\"} } }, { \"$project\": { \"posts\": { \"$filter\": { \"input\": \"?\", \"as\": \"post\", \"cond\": { \"$eq\": [ \"?\", \"?\" ] } } }, \"admin\": \"?\" } } ], \"cursor\": {}, \"lsid\": { \"id\": { \"$binary\": \"?\", \"$type\": \"?\" } }, \"$db\": \"test\" } }, \"result\": 0 }";
        final JsonObject mongoRedactedJsonObject = JsonParser.parseString(mongoRedactedString).getAsJsonObject();
        final JsonObject mongoJson = JsonParser.parseString(mongoString).getAsJsonObject();
        final JsonObject args = mongoJson.get("param").getAsJsonObject().get("args").getAsJsonObject();
        
        Parser.RedactWithExceptions(mongoJson); // overrides args in-place
        
        Assert.assertEquals("Collection name should not be redacted", args.get("$db").getAsString(), "test");
        Assert.assertEquals("Object should not be redacted", args.get("aggregate").getAsString(), "users"); 
        
        Assert.assertEquals(mongoRedactedJsonObject, mongoJson);
    }

    @Test
    public void testParseRecord() throws ParseException {
        Record record = Parser.parseRecord(mongoJson);
        Assert.assertEquals("2WoIDPhSTcKHrdJW4azoow==", record.getSessionId());
        Assert.assertEquals("test", record.getDbName());
        Assert.assertEquals(Parser.UNKOWN_STRING, record.getAppUserName());
        // TODO convert dateString to timestamp int

        Assert.assertNotNull(record.getSessionLocator());
    }

    @Test
    public void testParseData() {
        Data data = parser.parseData(mongoJson);
        Construct construct = data.getConstruct();
        Assert.assertNotNull(data);
        Assert.assertEquals("aggregate", construct.sentences.get(0).verb);
        Assert.assertEquals(mongoString.replace(" ", ""), construct.getFull_sql());
    }

    @Test
    public void testParseSessionLocator() throws ParseException {
        Record record = Parser.parseRecord(mongoJson);
        SessionLocator actual = record.getSessionLocator();

        Assert.assertEquals("127.0.0.1", actual.getServerIp());
        Assert.assertEquals(27017, actual.getServerPort());
        Assert.assertEquals("127.0.0.1", actual.getClientIp());
        Assert.assertEquals(56984, actual.getClientPort());

    }

    @Test
    public void testParseSessionLocator_IPv6() throws ParseException {
        final String mongoIPv6String = "{ \"atype\": \"authCheck\", \"ts\": { \"$date\": \"2020-01-26T09:58:44.547-0500\" }, \"local\": { \"ip\": \"2001:0db8:85a3:0000:0000:8a2e:0370:7334\", \"port\": 27017 }, \"remote\": { \"ip\": \"2001:0db8:85a3:0000:0000:8a2e:0370:7334\", \"port\": 56984 }, \"users\": [], \"roles\": [], \"param\": { \"command\": \"aggregate\", \"ns\": \"test.travelers\", \"args\": { \"aggregate\": \"travelers\", \"pipeline\": [ { \"$graphLookup\": { \"from\": \"airports\", \"startWith\": \"$nearestAirport\", \"connectFromField\": \"connects\", \"connectToField\": \"airport\", \"maxDepth\": 2, \"depthField\": \"numConnections\", \"as\": \"destinations\" } } ], \"cursor\": {}, \"lsid\": { \"id\": { \"$binary\": \"2WoIDPhSTcKHrdJW4azoow==\", \"$type\": \"04\" } }, \"$db\": \"test\" } }, \"result\": 0 }";
        final JsonObject mongoJson = JsonParser.parseString(mongoIPv6String).getAsJsonObject();

        Record record = Parser.parseRecord(mongoJson);
        SessionLocator actual = record.getSessionLocator();

        Assert.assertTrue("sessionLocator should mark that IPs are in IPv6", actual.isIpv6());
        Assert.assertEquals("2001:0db8:85a3:0000:0000:8a2e:0370:7334", actual.getServerIpv6());
        Assert.assertEquals(27017, actual.getServerPort());
        Assert.assertEquals("2001:0db8:85a3:0000:0000:8a2e:0370:7334", actual.getClientIpv6());
        Assert.assertEquals(56984, actual.getClientPort());

    }

    @Test
    public void testParseAccessor() throws ParseException {
        Record record = Parser.parseRecord(mongoJson);
        Accessor actual = record.getAccessor();
        Assert.assertEquals(Parser.DATA_PROTOCOL_STRING, actual.getDbProtocol());
        Assert.assertEquals(Parser.SERVER_TYPE_STRING, actual.getServerType());

        Assert.assertEquals("", actual.getDbUser());

    }

    @Test
    public void testParseAccessor_nUsers() {
        final String testString = "{ 'atype': 'authCheck', 'ts': { '$date': '2020-01-26T09:58:44.547-0500' }, 'local': { 'ip': '127.0.0.1', 'port': 27017 }, 'remote': { 'ip': '127.0.0.1', 'port': 56984 }, 'users': [{'user': 'tal', 'db': 'test'}, {'user': 'talb', 'db': 'bios'}], 'roles': [], 'param': { 'command': 'aggregate', 'ns': 'test.travelers', 'args': { 'aggregate': 'travelers', 'pipeline': [ { '$graphLookup': { 'from': 'airports', 'startWith': '$nearestAirport', 'connectFromField': 'connects', 'connectToField': 'airport', 'maxDepth': 2, 'depthField': 'numConnections', 'as': 'destinations' } } ], 'cursor': {}, 'lsid': { 'id': { '$binary': '2WoIDPhSTcKHrdJW4azoow==', '$type': '04' } }, '$db': 'test' } }, 'result': 0 }";
        final JsonObject testJson = JsonParser.parseString(testString).getAsJsonObject();
        Accessor actual = parser.parseAccessor(testJson);

        Assert.assertEquals("tal talb ", actual.getDbUser());
    }

    @Test
    public void testParseTimestamp() {
        String date = Parser.parseTimestamp(mongoJson);
        Assert.assertEquals("2020-01-26T09:58:44.547-0500", date);
    }

    @Test
    public void testGetTime() throws ParseException {
        String dateString = Parser.parseTimestamp(mongoJson);
        long time = Parser.getTime(dateString);
        
        final String testString = "{ 'atype': 'authCheck', 'ts': { '$date': '2020-01-26T09:58:44.640-0500' }, 'local': { 'ip': '127.0.0.1', 'port': 27017 }, 'remote': { 'ip': '127.0.0.1', 'port': 56984 }, 'users': [{'user': 'tal', 'db': 'test'}, {'user': 'talb', 'db': 'bios'}], 'roles': [], 'param': { 'command': 'aggregate', 'ns': 'test.travelers', 'args': { 'aggregate': 'travelers', 'pipeline': [ { '$graphLookup': { 'from': 'airports', 'startWith': '$nearestAirport', 'connectFromField': 'connects', 'connectToField': 'airport', 'maxDepth': 2, 'depthField': 'numConnections', 'as': 'destinations' } } ], 'cursor': {}, 'lsid': { 'id': { '$binary': '2WoIDPhSTcKHrdJW4azoow==', '$type': '04' } }, '$db': 'test' } }, 'result': 0 }";
        final JsonObject testJson = JsonParser.parseString(testString).getAsJsonObject();
        String dateString2 = Parser.parseTimestamp(testJson);
        long time2 = Parser.getTime(dateString2);

        Assert.assertNotEquals(time, time2);
    }



}