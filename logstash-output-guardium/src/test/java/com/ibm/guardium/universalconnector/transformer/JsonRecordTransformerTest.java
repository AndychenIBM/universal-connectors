package com.ibm.guardium.universalconnector.transformer;


import org.junit.Assert;
import org.junit.Test;

public class JsonRecordTransformerTest {
    public static int convert_ipstr_to_int(String ip){
        int ret = 0;
        String[] segs = ip.split("\\.");

        if (segs!=null && segs.length>0){

            for (int i = segs.length; i > 0; i--){
                int value = 0;
                try{
                    value = Integer.valueOf(segs[i]);
                    value = value << 8*i;
                }catch (java.lang.Exception e){

                }
                ret += value ;
            }
        }
        return ret;
    }

    @Test
    public void testSessionLocatorCreation(){

        String ipStr = "9.70.147.59";
        JsonRecordTransformer transformer = new JsonRecordTransformer();
        int val = transformer.convert_ipstr_to_int(ipStr);

        Assert.assertEquals(val,999507465);
        System.out.println(1);
        //JsonRecordTransformer transformer = new JsonRecordTransformer();
        System.out.println(2);
        String recordStr = "{\"server_hostname\" : \"qa-db51\",\n" +
                "                \"server_ip\" : \"127.0.0.1\",\n" +
                "               \"@timestamp\" : 2020-02-03T15:52:58.857Z,\n" +
                "                \"operation\" : \"find\",\n" +
                "                \"client_ip\" : \"127.0.0.1\",\n" +
                "                 \"@version\" : \"1\",\n" +
                "              \"server_port\" : \"27017\",\n" +
                "               \"session_id\" : \"{\\\"id\\\":{\\\"$type\\\":\\\"04\\\",\\\"$binary\\\":\\\"x9TSJiiQRvqVuupi3W/eCA==\\\"}}\",\n" +
                "                \"timestamp\" : \"2020-02-03T10:52:58.858-0500\",\n" +
                "                \"Construct\" : \"{\\n  \\\"sentences\\\": [\\n    {\\n      \\\"verb\\\": \\\"find\\\",\\n      \\\"objects\\\": [\\n        {\\n          \\\"name\\\": \\\"inventory\\\",\\n          \\\"type\\\": \\\"collection\\\",\\n          \\\"fields\\\": [],\\n          \\\"schema\\\": \\\"\\\"\\n        }\\n      ],\\n      \\\"descendants\\\": [],\\n      \\\"fields\\\": []\\n    }\\n  ],\\n  \\\"full_sql\\\": null,\\n  \\\"original_sql\\\": null\\n}\",\n" +
                "              \"db_protocol\" : \"MONGODB WIRE PROTOCOL\",\n" +
                "                 \"database\" : \"test.inventory\",\n" +
                "             \"db_user_name\" : \"\",\n" +
                "                     \"type\" : \"syslog\",\n" +
                "                    \"users\" : [],\n" +
                "              \"server_type\" : \"MONGODB\",\n" +
                "            \"resource_type\" : \"Collection\",\n" +
                "            \"resource_name\" : \"inventory\",\n" +
                "           \"exception_type\" : 0,\n" +
                "              \"client_port\" : \"61126\",\n" +
                "    \"operation_args_filter\" : \"{\\\"item\\\":\\\"canvas\\\"}\"}";

        System.out.println(3);
//       a Record record = new Gson().fromJson(recordStr, Record.class);
//
//        System.out.println(4);
//        // build session start msg
//        Dtasource.Accessor  accessor = transformer.buildAccessor(record); //todo: handle differently error/exception record - do not put data_type in accessor

        System.out.println(5);
    }
}
