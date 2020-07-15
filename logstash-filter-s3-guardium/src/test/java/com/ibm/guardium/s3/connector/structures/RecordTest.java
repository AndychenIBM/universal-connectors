package com.ibm.guardium.s3.connector.structures;

import org.junit.Assert;
import org.junit.Test;

public class RecordTest {

    Record record = new Record();

    @Test 
    public void testAccessorPostManipulation() {
        String actual = "dummy-server";
        record.setAccessor(new Accessor());
        record.getAccessor().setServerHostName(actual);
        Assert.assertEquals(record.getAccessor().getServerHostName(), actual);
    }
}