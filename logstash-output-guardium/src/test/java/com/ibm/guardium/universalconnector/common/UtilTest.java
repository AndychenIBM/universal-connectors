package com.ibm.guardium.universalconnector.common;

import com.ibm.guardium.universalconnector.commons.structures.Time;
import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import com.ibm.guardium.proto.datasource.Datasource.Timestamp;

public class UtilTest {

    @Test
    public void testGetTimestamp() throws Exception {
        Instant instant = Instant.now();
        ZonedDateTime zonedInstant = instant.atZone(ZoneId.systemDefault());
        Time time = new Time(instant.toEpochMilli(), zonedInstant.getOffset().getTotalSeconds()/60, 0);

        Timestamp timestamp = Utilities.getTimestamp(time);
        
        Assert.assertEquals(instant.toEpochMilli()%1000* 1000, timestamp.getUsec());
        
        Assert.assertEquals(instant.toEpochMilli()/1000, timestamp.getUnixTime());
    }

    @Test
    public void testGetMicroseconds() throws Exception {
        Date date = new Date(); 
        
        Assert.assertEquals(
            "Milliseconds should be converted properly to microseconds to match proto format", 
            Utilities.getTimeMicroseconds(date.getTime()), date.getTime()%1000 * 1000);
    }
}
