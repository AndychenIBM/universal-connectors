package com.ibm.guardium.universalconnector.common;

import com.ibm.guardium.proto.datasource.Datasource;
import com.ibm.guardium.proto.datasource.Datasource.Timestamp;

public class Util {
    public static int getTimeUnixTime(Long time) {
        return (int) (time/1000);
    }

    public static int getTimeMicroseconds(Long time) {
        return (int) (time%1000 * 1000);
    }

    public static Timestamp getTimestamp(Long time) {
        return Datasource.Timestamp.newBuilder()
            .setUnixTime(Util.getTimeUnixTime(time))
            .setUsec(Util.getTimeMicroseconds(time))
            .build();
    }
}