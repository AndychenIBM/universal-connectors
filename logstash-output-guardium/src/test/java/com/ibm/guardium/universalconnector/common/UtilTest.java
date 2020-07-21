package com.ibm.guardium.universalconnector.common;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

import com.ibm.guardium.proto.datasource.Datasource.Timestamp;

public class UtilTest {

    @Test
    public void testGetTimestamp() throws Exception {
        Date date = new Date(); 
        Timestamp timestamp = Utilities.getTimestamp(date.getTime());
        
        Assert.assertEquals(
            Utilities.getTimeMicroseconds(date.getTime()),
            timestamp.getUsec());
        
        Assert.assertEquals(
            Utilities.getTimeUnixTime(date.getTime()),
            timestamp.getUnixTime());
    }

    @Test
    public void testGetMicroseconds() throws Exception {
        Date date = new Date(); 
        
        Assert.assertEquals(
            "Milliseconds should be converted properly to microseconds to match proto format", 
            Utilities.getTimeMicroseconds(date.getTime()), date.getTime()%1000 * 1000);
    }
}
