package com.ibm.guardium.universalconnector.status;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;


public class AgentStatusGenerator {
    private static Logger log = LogManager.getLogger(AgentStatusGenerator.class);
    public static final long NUMBER_OF_CYCLES_TO_ALERT = 10;
    private static final long CONSUMER_Q_FULL_LEVEL = 90;
    private long gap = 0;
    private int qSize;
    private String workerError = null;

    public AgentStatusGenerator(int msgQSize){
        qSize = msgQSize;
    }

    private AgentStatus processStatus(StatusType status, String comment) {
        return new AgentStatus().withStatus(status.name()).withComment(comment);
    }

    private AgentStatus createStreamingLagBehindStatus() {
        String comment = "status.comment.consumer_falling_behind";
        return processStatus(StatusType.RED, comment);
    }

    private Boolean isStreamingLagBehind(Map<String, String> stats,  Map<String, Long> context) {
        long current = Long.parseLong(stats.getOrDefault("IN_CONSUMER_ITERATOR_AGE_MILLISECONDS", "0"));
        long inConsumerIteratorAgeMillisecondsLastValue = context.getOrDefault("inConsumerIteratorAgeMillisecondsLastValue", 0L);
        context.put("inConsumerIteratorAgeMillisecondsLastValue", current);
        long currentGap = current - inConsumerIteratorAgeMillisecondsLastValue;
        if (currentGap <= gap) {
            // we are in steady state or gaining.
            gap = currentGap;
            context.put("inConsumerIteratorAgeMillisecondsCountDecline", 0L);
            return false;
        }
        // we are falling behind. not good. check if this changes.
        gap = currentGap;
        long inConsumerIteratorAgeMillisecondsCountDecline = context.getOrDefault("inConsumerIteratorAgeMillisecondsCountDecline", 0L) + 1;
        context.put("inConsumerIteratorAgeMillisecondsCountDecline", inConsumerIteratorAgeMillisecondsCountDecline);
        return (inConsumerIteratorAgeMillisecondsCountDecline > NUMBER_OF_CYCLES_TO_ALERT);
    }

    private AgentStatus createAgentQFullStatus() {
        String comment = "status.comment.consumer_q_full";
        return processStatus(StatusType.RED, comment);
    }

    private Boolean isQAtFullLevel(long qUsed){
        return ((qUsed / qSize) * 100 > CONSUMER_Q_FULL_LEVEL);
    }

    private Boolean isAgentQFull(Map<String, String> stats, Map<String, Long> context) {
        long qUsed = Long.parseLong(stats.getOrDefault("OUT_CONSUMER_RECORDS_IN_QUEUE", "0"));
        if (qUsed == 0)
            return false;
        long numRecordsSent =  Long.parseLong(stats.getOrDefault("OUT_CONSUMER_RECORDS_COUNT_SUM", "0"));
        long prevNumRecordsSent =  context.getOrDefault("prevNumRecordsSent", 0L);
        long numCyclesQFull = context.getOrDefault("numCyclesQFull", 0L) + 1;
        context.put("prevNumRecordsSent", numRecordsSent);

        if (isQAtFullLevel(qUsed)) {
            context.put("numCyclesQFull", numCyclesQFull);
            if (numRecordsSent == prevNumRecordsSent) {
                return true; // no sent records in last cycle.
            }
            return (numCyclesQFull > NUMBER_OF_CYCLES_TO_ALERT);
        }
        context.put("numCyclesQFull", 0L);
        return false;
    }

    private Boolean isAgentOutConnectionFailing(Map<String, String> stats, Map<String, Long> context){
        long current = Long.parseLong(stats.getOrDefault("OUT_CONSUMER_FAILURES", "0"));
        long consumerOutConnectionFailures = context.getOrDefault("consumerOutConnectionFailures", 0L);
        if (current > consumerOutConnectionFailures){
            context.put("consumerOutConnectionFailures", current);
            return true;
        }
        return false;
    }

    private AgentStatus createConsumerOutConnectionFailingStatus() {
        return processStatus(StatusType.RED, "status.comment.consumerOutConnectionFailing");
    }

    private AgentStatus createWorkerErrorStatus() {
        return processStatus(StatusType.RED, workerError);
    }

    private Boolean isWorkerError() {
        //TODO:
//        workerError = GuardAuditAgent.getInstance().workerErrorStatus(null, false);
        return (workerError != null);
    }

    private Boolean isNoInboundRecords(Map<String, String> stats,  Map<String, Long> context){
        long current = Long.parseLong(stats.getOrDefault("IN_CONSUMER_RECORDS_COUNT_SUM", "0"));
        long inboundRecords = context.getOrDefault("inboundRecords", 0L);
        context.put("inboundRecords", current);
        if (current > inboundRecords){
            context.put("noInboundRecordsCycleCount", 0L);
            return false;
        }
        long noInboundRecordsCycleCount = context.getOrDefault("noInboundRecordsCycleCount", 0L) +1;
        context.put("noInboundRecordsCycleCount", noInboundRecordsCycleCount);
        return (noInboundRecordsCycleCount > NUMBER_OF_CYCLES_TO_ALERT);
    }

    //
    private AgentStatus createNoInboundRecordsStatus() {
        return processStatus(StatusType.YELLOW, "status.comment.consumerNoInboundRecords");
    }

    public AgentStatus generateStatus(Map<String, String> stats, Map<String, Long> context) {
        if(stats.containsKey("Exception"))
            return processStatus(StatusType.RED, stats.get("Exception"));

        if(isWorkerError())
            return createWorkerErrorStatus();

        if(isAgentOutConnectionFailing(stats, context))
            return createConsumerOutConnectionFailingStatus();

        if(isAgentQFull(stats, context))
            return createAgentQFullStatus();

        if(isStreamingLagBehind(stats, context))
            return createStreamingLagBehindStatus();

        if(isNoInboundRecords(stats, context))
            return createNoInboundRecordsStatus();

        return processStatus(StatusType.GREEN, "status.comment.allgood");
    }

    public AgentStatus getStoppedStatus() {
        if(isWorkerError())
            return createWorkerErrorStatus();
        return processStatus(StatusType.BLUE, "status.comment.stopped");
    }

    public AgentStatus getFailedStatusStatus() {
        return processStatus(StatusType.RED, "status.comment.failedToAccess");
    }

    public AgentStatus getWorkerErrorStatus(String error) {
        return processStatus(StatusType.RED, error);
    }
}
