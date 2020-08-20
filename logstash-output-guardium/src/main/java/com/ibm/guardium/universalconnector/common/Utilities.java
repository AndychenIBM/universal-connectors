package com.ibm.guardium.universalconnector.common;

import com.ibm.guardium.proto.datasource.Datasource;
import com.ibm.guardium.proto.datasource.Datasource.Timestamp;
import com.ibm.guardium.universalconnector.common.structures.Time;

public class Utilities {
    public static int getTimeUnixTime(Long time) {
        return (int) (time/1000);
    }

    public static int getTimeMicroseconds(Long time) {
        return (int) (time%1000 * 1000);
    }

    public static Timestamp getTimestamp(Time time) {
        return Datasource.Timestamp.newBuilder()
                .setUnixTime(getTimeUnixTime(time.getTimstamp()))
                .setUsec(getTimeMicroseconds(time.getTimstamp()))
                .setTzMin(time.getMinOffsetFromGMT())
                .setTzDst(time.getMinDst())
                .build();
    }
}