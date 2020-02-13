package com.ibm.guardium.universalconnector.agent;


import com.google.protobuf.Message;
import com.ibm.guardium.universalconnector.config.SnifferConfig;
import com.ibm.guardium.universalconnector.config.UCConfig;
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
    private UCConfig ucConfig;
    private SnifferConfig snifferConfig;
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


    public Agent(UCConfig ucConfig, SnifferConfig snifferConfig ) {
        this.ucConfig = ucConfig;
        this.snifferConfig = snifferConfig;
        messageQueue = new ArrayBlockingQueue<>(CONSUMER_Q_SIZE);
        transmitterStatsCollector = new TransmitterStatsCollector();
        currentStats = transmitterStatsCollector.getCurrent();
        recordTransmitter = new GuardConnection(messageQueue, currentStats);
    }

    class StatsTimer extends TimerTask {
        public void run() {
            Thread.currentThread().setName(ucConfig.getConnectorId() + "-StatsTimer");
            currentStats.setRecordsInQ(messageQueue.size());
            transmitterStatsCollector.runCollectorTasks();
        }
    }

    public UCConfig getUcConfig() {
        return ucConfig;
    }
    public SnifferConfig getSnifferConfig() {
        return snifferConfig;
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
                log.warn("messageQueue still contains records");
                return false;
            }
            waitTime += 1;
            Thread.sleep(1000);
        }
        return true;
    }

    public void start() throws Exception{
        recordTransmitter.setup(ucConfig, snifferConfig);
        log.debug("ready to create connection to snif");
        connThread = new Thread(recordTransmitter);
        connThread.setName(ucConfig.getConnectorId() + "-recordTransmitter");
        connThread.start();
        log.info("waiting for connection to snif to open");
        int retries = CONNECTION_RETRIES;
        while(!recordTransmitter.isStatusOpen() && retries>0){
            Thread.sleep(1000);
            retries--;
        }
        if (retries==0 && !recordTransmitter.isStatusOpen()){
            throw new Exception("Failed to connecto to sniffer "+snifferConfig);
        }
        log.debug("Starting stats timer.");
        timer = new Timer();
        StatusWriter statusWriter = getStatusWriter(ucConfig);
        statusWriter.init();
        this.agentStatusGenerator = new AgentStatusGenerator(CONSUMER_Q_SIZE);
        statsTimer = new StatsTimer();
        timer.scheduleAtFixedRate(statsTimer, 0, 1000);
        state = AgentState.STARTED;
    }

    private StatusWriter getStatusWriter(UCConfig ucConfig) throws Exception{
        StatusWriterFactory statusWriterFactory = new StatusWriterFactory();

        return statusWriterFactory.Build(ucConfig);
    }

//    private StatsWriter getStatsWriters(Properties properties) {
//        StatsWriterFactory statsWriterFactory = new StatsWriterFactory();
//        return statsWriterFactory.Build(properties.getProperty("consumer.ucConfig.statusWriterType"),
//                ucConfig.getPrimaryConsumerIp());
//    }

    private void waitForMessageQueue() throws InterruptedException{
        if (messageQueue.remainingCapacity() > 0)
            return;
        log.debug("messageQueue is full, waiting.");
        while(messageQueue.remainingCapacity() == 0)
            Thread.sleep(1);
    }

    public void postFailedStatus(UCConfig ucConfig) throws Exception{
        AgentStatus status = agentStatusGenerator.getFailedStatusStatus();
        StatusWriter sw = getStatusWriter(ucConfig);
        sw.updateStatus(status.getStatus(), status.getComment());
    }
    public void send(Message msg) {
        if (isStopStart) {
            log.trace("Agent is stop/start, and not accepting records");
            return;
        }
        try {
            waitForMessageQueue();
            messageQueue.put(new QueuedMessage(msg));
            log.debug("Agent queue size (proto message was added): " + messageQueue.size());
        } catch (Exception e) {
            log.error(e);
        }
    }

    public void send(byte[] msg) {
        String errMsg;
        if (isStopStart) {
            log.trace("Agent is stop/start, and not accepting records");
            return;
        }
        if (msg.length >= GuardConnection.INIT_BUF_LEN){
            errMsg = "msg length:" + msg.length + " is more then max length: " + GuardConnection.INIT_BUF_LEN;
            throw new GuardUCException(errMsg);
        }
        try {
            waitForMessageQueue();
            messageQueue.put(new QueuedMessage(msg));
            log.debug("Agent queue size (byte[] message was added): " + messageQueue.size());
        } catch (Exception e) {
            log.error(e);
        }
    }

    public void stop() {
        try {
            stopTimer();
            stopConnection();
            log.debug("Agent:" + ucConfig.getConnectorId() + " stopped");
        } catch (InterruptedException e) { //don't think there will be this exception
            log.error(e);
        }
    }

    private void stopConnection() throws InterruptedException {
        log.debug("stopping conn and waiting to join");
        connThread.interrupt();
        connThread.join();
    }

    private void stopTimer() {
        log.debug("Stopping timers.");
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
