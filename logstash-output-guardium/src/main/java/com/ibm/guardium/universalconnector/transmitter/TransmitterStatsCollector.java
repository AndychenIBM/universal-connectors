package com.ibm.guardium.universalconnector.transmitter;

import java.util.HashMap;
import java.util.Map;


public class TransmitterStatsCollector {
    private TransmitterStats current = new TransmitterStats();
    private TransmitterStats temp = new TransmitterStats();
    private TransmitterStats oneSecBack = new TransmitterStats();
    private TransmitterStats oneMinBack = new TransmitterStats();
    private TransmitterStats oneSecSums = new TransmitterStats();
    private TransmitterStats oneMinSums = new TransmitterStats();
    private int index = 0;

    public void runCollectorTasks() {
        current.copyTo(temp);
        oneSecDiff();
        if (index % 60 == 0)
            oneMinDiff();
        index++;
    }

    private void diff(TransmitterStats target, TransmitterStats old){
        target.setIncomingRecords(temp.getIncomingRecords() - old.getIncomingRecords());
        target.setMsgsSent(temp.getMsgsSent() - old.getMsgsSent());
        target.setReconnects(temp.getReconnects() - old.getReconnects());
        target.setErrors(temp.getErrors() - old.getErrors());
        target.setBytesSent(temp.getBytesSent() - old.getBytesSent());
        target.setLastReceiveTime(temp.getLastReceiveTime() - old.getLastReceiveTime());
        target.setLastMsgCreateTime(temp.getLastMsgCreateTime() - old.getLastMsgCreateTime());
        target.setRecordsInQ(temp.getRecordsInQ() - old.getRecordsInQ());
    }

    private void oneSecDiff() {
        diff(oneSecSums, oneSecBack);
        temp.copyTo(oneSecBack);
    }

    private void oneMinDiff() {
        diff(oneMinSums, oneMinBack);
        temp.copyTo(oneMinBack);
    }

    private Map<String, String> getStatsMap(TransmitterStats stats){
        Map<String, String> map = new HashMap<>();
        map.put("IN_CONSUMER_RECORDS_COUNT_SUM", String.valueOf(stats.getIncomingRecords()));
        map.put("OUT_CONSUMER_RECORDS_COUNT_SUM", String.valueOf(stats.getMsgsSent()));
        map.put("OUT_CONSUMER_RECORDS_BYTES_SUM", String.valueOf(stats.getBytesSent()));
        map.put("OUT_CONSUMER_FAILURES", String.valueOf(stats.getErrors()));
        map.put("OUT_CONSUMER_RECONNECTS", String.valueOf(stats.getReconnects()));
        map.put("OUT_CONSUMER_RECORDS_IN_QUEUE", String.valueOf(stats.getRecordsInQ() ));
        map.put("IN_CONSUMER_ITERATOR_AGE_MILLISECONDS", String.valueOf(stats.getLastMsgCreateTime()));
        return map;
    }

    public Map<String, String> getOneSecondStatsMap(){
        return getStatsMap(oneSecSums);
    }

    public Map<String, String> getOneMinuteStatsMap(){
        return getStatsMap(oneMinSums);
    }

    public TransmitterStats getCurrent() {
        return current;
    }
}
