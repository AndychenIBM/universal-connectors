package com.ibm.guardium.universalconnector.transformer;


import com.google.gson.Gson;
import com.ibm.guardium.proto.datasource.Datasource;
import com.ibm.guardium.universalconnector.transformer.jsonrecord.Accessor;
import com.ibm.guardium.universalconnector.transformer.jsonrecord.Record;
import org.jruby.RubyProcess;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class JsonRecordTransformerTest {
    private static String recordString = "{\n" +
            "\t\t\"sessionId\":\"n/a\",\n" +
            "\t\t\"dbName\":\"students\",\n" +
            "\t\t\"appUserName\":\"n/a\",\n" +
            "\t\t\"time\":1581841318,\n" +
            "\t\t\"sessionLocator\":{\n" +
            "\t\t\t\"clientIp\":\"127.0.20.1\",\n" +
            "\t\t\t\"clientPort\":1234,\n" +
            "\t\t\t\"serverIp\":\"127.0.30.1\",\n" +
            "\t\t\t\"serverPort\":5678,\n" +
            "\t\t\t\"isIpv6\":false,\n" +
            "\t\t\t\"clientIpv6\":\"n/a\",\n" +
            "\t\t\t\"serverIpv6\":\"n/a\"\n" +
            "\t\t},\n" +
            "\t\t\"accessor\":{\n" +
            "\t\t\t\"dbUser\":\"admin\",\n" +
            "\t\t\t\"serverType\":\"MONGODB\",\n" +
            "\t\t\t\"serverOs\":\"n/a\",\n" +
            "\t\t\t\"clientOs\":\"n/a\",\n" +
            "\t\t\t\"clientHostName\":\"n/a\",\n" +
            "\t\t\t\"serverHostName\":\"qa-db51\",\n" +
            "\t\t\t\"commProtocol\":\"n/a\",\n" +
            "\t\t\t\"dbProtocol\":\"Logstash\",\n" +
            "\t\t\t\"dbProtocolVersion\":\"n/a\",\n" +
            "\t\t\t\"osUser\":\"n/a\",\n" +
            "\t\t\t\"sourceProgram\":\"mongod\",\n" +
            "\t\t\t\"client_mac\":\"n/a\",\n" +
            "\t\t\t\"serverDescription\":\"n/a\",\n" +
            "\t\t\t\"serviceName\":\"abcservicename\",\n" +
            "\t\t\t\"language\":\"FREE_TEXT\",\n" +
            "\t\t\t\"type\":\"CONSTRUCT\"\n" +
            "\t\t},\n" +
            "\t\t\"data\":{\n" +
            "\t\t\t\"timestamp\":0,\n" +
            "\t\t\t\"useConstruct\":false,\n" +
            "\t\t\t\"originalSqlCommand\":\"\",\n" +
            "\t\t\t\"construct\":{\n" +
            "\t\t\t\t\"sentences\":[\n" +
            "\t\t\t\t\t{\n" +
            "\t\t\t\t\t\t\"verb\":\"find\",\n" +
            "\t\t\t\t\t\t\"objects\":[{\"name\":\"transactions\",\"type\":\"collection\",\"fields\":[],\"schema\":\"\"}],\n" +
            "\t\t\t\t\t\t\"descendants\":[],\n" +
            "\t\t\t\t\t\t\"fields\":[]\n" +
            "\t\t\t\t\t}\n" +
            "\t\t\t\t],\n" +
            "\t\t\t\t\"full_sql\":\" { \\\"atype\\\" : \\\"authCheck\\\", \\\"ts\\\" : { \\\"$date\\\" : \\\"2020-02-16T03:21:58.185-0500\\\" }, \\\"local\\\" : { \\\"ip\\\" : \\\"127.0.30.1\\\", \\\"port\\\" : 0 }, \\\"remote\\\" : { \\\"ip\\\" : \\\"127.0.20.1\\\", \\\"port\\\" : 0 }, \\\"users\\\" : [], \\\"roles\\\" : [], \\\"param\\\" : { \\\"command\\\" : \\\"find\\\", \\\"ns\\\" : \\\"config.transactions\\\", \\\"args\\\" : { \\\"find\\\" : \\\"transactions\\\", \\\"filter\\\" : { \\\"lastWriteDate\\\" : { \\\"$lt\\\" : { \\\"$date\\\" : \\\"2020-02-16T02:51:58.185-0500\\\" } } }, \\\"projection\\\" : { \\\"_id\\\" : 1 }, \\\"sort\\\" : { \\\"_id\\\" : 1 }, \\\"$db\\\" : \\\"config\\\" } }, \\\"result\\\" : 0 }\"\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t}\n";

    @Test
    public void testParsingRecordToJson(){

        Record record = (new Gson()).fromJson(recordString, Record.class);

        Assert.assertTrue("failed to parse record string to json", record!=null);
    }

     @Test
    public void testIpTranslation() {

        JsonRecordTransformer transformer = new JsonRecordTransformer();
        int val = transformer.convert_ipstr_to_int("9.70.147.59");
        Assert.assertEquals("Failed to parse 9.70.147.59",val,999507465);

        val = transformer.convert_ipstr_to_int("127.0.0.1");
        Assert.assertEquals("Failed to parse 127.0.0.1",val,16777343);

        val = transformer.convert_ipstr_to_int("0.0.0.0");
        Assert.assertEquals("Failed to parse 0.0.0.0",val,0);
    }

    @Test
    public void testRecordTrasformation(){

        Record record = new Gson().fromJson(recordString, Record.class);

        JsonRecordTransformer transformer = new JsonRecordTransformer();

        List<Datasource.Guard_ds_message> messages = transformer.transform(recordString);
        Datasource.Session_start session_start = messages.get(0).getSessionStart();

    }

    @Test
    public void testSessionLocatorCreation(){
        JsonRecordTransformer transformer = new JsonRecordTransformer();
        Record record = new Gson().fromJson(recordString, Record.class);
        Datasource.Session_locator   sessionLocator= transformer.buildSessionLocator(record);
        Assert.assertTrue("invalid client ip", sessionLocator.getClientIp()==18088063);
        Assert.assertTrue("invalid client port", sessionLocator.getClientPort()==1234);
        Assert.assertTrue("invalid server ip", sessionLocator.getServerIp()==18743423);
        Assert.assertTrue("invalid server port", sessionLocator.getServerPort()==5678);
    }

    @Test
    public void testAccessorCreation(){
        JsonRecordTransformer transformer = new JsonRecordTransformer();
        // standard "no special cases" test
        Record record = new Gson().fromJson(recordString, Record.class);
        Accessor ra = record.getAccessor();
        Datasource.Accessor accessor = transformer.buildAccessor(record);
        Assert.assertTrue("invalid dbUser", accessor.getDbUser().equals("admin"));
        Assert.assertTrue("invalid serverType", accessor.getServerType().equals("MONGODB"));
        Assert.assertTrue("invalid serverOs", accessor.getServerOs().equals(ra.getServerOs()));
        Assert.assertTrue("invalid serverOs", accessor.getClientHostname().equals(ra.getClientHostName()));
        Assert.assertTrue("invalid serverHostname", accessor.getServerHostname().equals(ra.getServerHostName()));
        Assert.assertTrue("invalid commProtocol", accessor.getCommProtocol().equals(ra.getCommProtocol()));
        Assert.assertTrue("invalid DbProtocol", accessor.getDbProtocol().equals(ra.getDbProtocol()));
        Assert.assertTrue("invalid DbProtocolVersion", accessor.getDbProtocolVersion().equals(ra.getDbProtocolVersion()));
        Assert.assertTrue("invalid sourceProgram", accessor.getSourceProgram().equals(ra.getSourceProgram()));
        Assert.assertTrue("invalid serverDescription", accessor.getServerDescription().equals(ra.getServerDescription()));
        Assert.assertTrue("invalid serviceName", accessor.getServiceName().equals(ra.getServiceName()));
        Assert.assertTrue("invalid language", accessor.getLanguage().equals(Datasource.Application_data.Language_type.valueOf(ra.getLanguage().toUpperCase())));
        Assert.assertTrue("invalid dataType", accessor.getType().equals(Datasource.Application_data.Data_type.CONSTRUCT));
        Assert.assertTrue("invalid datasourceType", accessor.getDatasourceType().equals(Datasource.Application_data.Datasource_type.UNI_CON));


        // empty fields test
        String accessorString ="{\n" +
                "\t\t\t\"dbUser\":\"admin\",\n" +
                "\t\t\t\"serverType\":\"MONGODB\",\n" +
                "\t\t\t\"clientOs\":\"n/a\",\n" +
                "\t\t\t\"clientHostName\":\"n/a\",\n" +
                "\t\t\t\"serverHostName\":\"qa-db51\",\n" +
                "\t\t\t\"commProtocol\":\"n/a\",\n" +
                "\t\t\t\"dbProtocol\":\"Logstash\",\n" +
                "\t\t\t\"dbProtocolVersion\":\"n/a\",\n" +
                "\t\t\t\"osUser\":\"n/a\",\n" +
                "\t\t\t\"sourceProgram\":\"mongod\",\n" +
                "\t\t\t\"client_mac\":\"n/a\",\n" +
                "\t\t\t\"serverDescription\":\"n/a\",\n" +
                "\t\t\t\"serviceName\":\"abcservicename\",\n" +
                "\t\t\t\"language\":\"FREE_TEXT\",\n" +
                "\t\t\t\"type\":\"CONSTRUCT\"\n" +
                "\t\t}";
        ra = new Gson().fromJson(accessorString, Accessor.class);
        record = new Record();
        record.setAccessor(ra);

    }

}
