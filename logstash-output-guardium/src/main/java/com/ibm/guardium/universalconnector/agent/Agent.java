package com.ibm.guardium.universalconnector.agent;


import com.google.protobuf.Message;
import com.ibm.guardium.universalconnector.config.ConnectionConfig;
import com.ibm.guardium.universalconnector.exceptions.GuardUCException;
import com.ibm.guardium.universalconnector.status.AgentStatus;
import com.ibm.guardium.universalconnector.status.AgentStatusGenerator;
import com.ibm.guardium.universalconnector.status.StatusWriter;
import com.ibm.guardium.universalconnector.status.StatusWriterFactory;
import com.ibm.guardium.universalconnector.transmitter.*;
import com.ibm.guardium.universalconnector.transmitter.socket.GuardConnection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class Agent {
    private static Log log = LogFactory.getLog(Agent.class);

    private AgentState state = AgentState.STOPPED;
    //private UCConfig ucConfig;
    //private SnifferConfig snifferConfig;
    private ConnectionConfig config;
    private boolean isStopStart = false;
    private boolean isMarkedForRemoval = false;
    private BlockingQueue<QueuedMessage> messageQueue;
    private RecordTransmitter recordTransmitter;
    private TransmitterStatsCollector transmitterStatsCollector;
    private TransmitterStats currentStats;
    private AgentStatusGenerator agentStatusGenerator;
    private Timer timer;
    private StatsTimer statsTimer;
    private Thread connThread = null;
    private String workerError;
    private final int CONSUMER_Q_SIZE = 1000;
    private int CONNECTION_RETRIES = 10;


    public Agent(ConnectionConfig config ) {
        this.config = config;
        messageQueue = new ArrayBlockingQueue<>(CONSUMER_Q_SIZE);
        transmitterStatsCollector = new TransmitterStatsCollector();
        currentStats = transmitterStatsCollector.getCurrent();
        recordTransmitter = new GuardConnection(messageQueue, currentStats);
    }

    class StatsTimer extends TimerTask {
        public void run() {
            Thread.currentThread().setName(config.getId() + "-StatsTimer");
            currentStats.setRecordsInQ(messageQueue.size());
            transmitterStatsCollector.runCollectorTasks();
        }
    }

    public String getId() {
        return config.getId();
    }

    public boolean isStopStart() {
        return isStopStart;
    }

    public void setStopStart(boolean stopStart) {
        isStopStart = stopStart;
    }

    public boolean isMarkedForRemoval() {
        return isMarkedForRemoval;
    }

    public void setMarkedForRemoval(boolean markedForRemoval) {
        isMarkedForRemoval = markedForRemoval;
    }

    public AgentState getState() {
        return state;
    }

    public void setState(AgentState state) {
        this.state = state;
    }

    public boolean waitForQToEmpty() throws InterruptedException{
        int waitTime = 0;
        while (!messageQueue.isEmpty()){
            if (waitTime > 15){
                log.warn("messageQueue still contains records "+config.getId());
                return false;
            }
            waitTime += 1;
            Thread.sleep(1000);
        }
        return true;
    }

    public void start() throws Exception{
        recordTransmitter.setup(config);
        StatusWriter statusWriter = getStatusWriter(config);
        statusWriter.init();
        this.recordTransmitter.setStatusWriter(statusWriter);

        log.debug("ready to create connection to snif "+config.getId());
        connThread = new Thread(recordTransmitter);
        connThread.setName(config.getId() + "-recordTransmitter");
        connThread.start();
        log.info("waiting for connection to snif to open"+config.getId());
        int retries = CONNECTION_RETRIES;
        while(!recordTransmitter.isStatusOpen() && retries>0){
            Thread.sleep(1000);
            retries--;
        }
        if (retries==0 && !recordTransmitter.isStatusOpen()){
            throw new Exception("Failed to connecto to sniffer "+config);
        }
        log.debug("Starting stats timer."+config.getId());
        timer = new Timer();
        this.agentStatusGenerator = new AgentStatusGenerator(CONSUMER_Q_SIZE);
        statsTimer = new StatsTimer();
        timer.scheduleAtFixedRate(statsTimer, 0, 1000);
        state = AgentState.STARTED;
    }

    private StatusWriter getStatusWriter(ConnectionConfig config) throws Exception{
        StatusWriterFactory statusWriterFactory = new StatusWriterFactory();

        return statusWriterFactory.Build(config);
    }

//    private StatsWriter getStatsWriters(Properties properties) {
//        StatsWriterFactory statsWriterFactory = new StatsWriterFactory();
//        return statsWriterFactory.Build(properties.getProperty("consumer.ucConfig.statusWriterType"),
//                ucConfig.getPrimaryConsumerIp());
//    }

    private void waitForMessageQueue() throws InterruptedException{
        if (messageQueue.remainingCapacity() > 0)
            return;
        if (log.isDebugEnabled()) {log.debug("messageQueue is full, waiting. "+config.getId());}
        while(messageQueue.remainingCapacity() == 0)
            Thread.sleep(1);
    }

    public void postFailedStatus(ConnectionConfig config) throws Exception{
        AgentStatus status = agentStatusGenerator.getFailedStatusStatus();
        StatusWriter sw = getStatusWriter(config);
        sw.updateStatus(status.getStatus(), status.getComment());
    }
    public void send(Message msg) {
        if (isStopStart) {
            log.trace("Agent is stop/start, and not accepting records "+config.getId());
            return;
        }
        try {
            waitForMessageQueue();
            messageQueue.put(new QueuedMessage(msg));
            if (log.isDebugEnabled()) {log.debug("Agent queue size (proto message was added): " + messageQueue.size()+" "+config.getId());}
        } catch (Exception e) {
            log.error(e);
        }
    }

    public void send(byte[] msg) {
        String errMsg;
        if (isStopStart) {
            log.trace("Agent is stop/start, and not accepting records "+config.getId());
            return;
        }
        if (msg.length >= GuardConnection.INIT_BUF_LEN){
            errMsg = "msg length:" + msg.length + " is more then max length: " + GuardConnection.INIT_BUF_LEN+" "+config.getId();
            throw new GuardUCException(errMsg);
        }
        try {
            waitForMessageQueue();
            messageQueue.put(new QueuedMessage(msg));
            if (log.isDebugEnabled()){log.debug("Agent queue size (byte[] message was added): " + messageQueue.size()+" "+config.getId());}
        } catch (Exception e) {
            log.error("Error sending message for "+config.getId(),e);
        }
    }

    public void stop() {
        try {
            stopTimer();
            stopConnection();
            log.debug("Agent:" + config.getId() + " stopped");
        } catch (InterruptedException e) { //don't think there will be this exception
            log.error(e);
        }
    }

    private void stopConnection() throws InterruptedException {
        log.debug("stopping conn and waiting to join "+config.getId());
        connThread.interrupt();
        connThread.join();
    }

    private void stopTimer() {
        log.debug("Stopping timers."+config.getId());
        timer.cancel();
    }

    public void setConnLastMsgCreateTime(long t){
        currentStats.setLastMsgCreateTime(t);
    }

    // one entry point to get/set worker error.
    // will set the err and return the previous error.
    public synchronized String workerErrorStatus(String err, Boolean fatal, Boolean set){
        if (set) {
            workerError = err;
        }
        /*
        if (fatal){
            return workerError;
        }
        if (fatal){
            fatalError = true;
        }
        String tmp = workerError;
        workerError = err;
        return tmp;
        */
        return workerError;
    }

    public synchronized void incIncomingRecordsCount(){
        currentStats.incrementIncomingRecords();
    }

}
