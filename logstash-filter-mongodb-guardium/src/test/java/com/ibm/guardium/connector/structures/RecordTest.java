package com.ibm.guardium.connector.structures;

import com.ibm.guardium.connector.structures.Accessor;
import com.ibm.guardium.connector.structures.Record;

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