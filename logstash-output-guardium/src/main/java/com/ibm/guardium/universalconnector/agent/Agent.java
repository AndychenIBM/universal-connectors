package com.ibm.guardium.universalconnector.agent;


import com.google.protobuf.Message;
import com.ibm.guardium.universalconnector.config.ConnectionConfig;
import com.ibm.guardium.universalconnector.status.AgentStatus;
import com.ibm.guardium.universalconnector.status.AgentStatusGenerator;
import com.ibm.guardium.universalconnector.status.StatusWriter;
import com.ibm.guardium.universalconnector.status.StatusWriterFactory;
import com.ibm.guardium.universalconnector.transmitter.*;
import com.ibm.guardium.universalconnector.transmitter.socket.GuardConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class Agent {
    private static Logger log = LogManager.getLogger(Agent.class);
    public enum AgentState
    {
        STOPPED,
        STARTED,
        RUNNING,
        ERROR;
    }

    private AgentState state = AgentState.STOPPED;

    //private UCConfig ucConfig;
    //private SnifferConfig snifferConfig;
    private ConnectionConfig config;
    private boolean isStopStart = false;
    private boolean isMarkedForRemoval = false;
    private BlockingQueue<QueuedMessage> messageQueue;
    private RecordTransmitter recordTransmitter;
    private TransmitterStatsCollector transmitterStatsCollector;
    //private TransmitterStats currentStats;
    private AgentStatusGenerator agentStatusGenerator;
    private Timer timer;
//    private StatsTimer statsTimer;
    private Thread connThread = null;
    private String workerError;
    private final int CONSUMER_Q_SIZE = 1000;
    private int CONNECTION_RETRIES = 10;

    private Object agentUpdateStateLock = new Object();

    public Agent(ConnectionConfig config ) {
        this.config = config;
        messageQueue = new ArrayBlockingQueue<>(CONSUMER_Q_SIZE);
        transmitterStatsCollector = new TransmitterStatsCollector();
        recordTransmitter = new GuardConnection(messageQueue, transmitterStatsCollector.getCurrent());
    }

    public ConnectionConfig getConfig() {
        return config;
    }

    class StatsTimer extends TimerTask {
        public void run() {
            Thread.currentThread().setName(config.getId() + "-StatsTimer");
            transmitterStatsCollector.getCurrent().setRecordsInQ(messageQueue.size());
            transmitterStatsCollector.runCollectorTasks();

            if ( transmitterStatsCollector.getCurrent().noDataForTooLongTime(config.getUcConfig().getNoDataThresholdInMs() )){
                log.debug("It has been more then TIMELIMIT ("+config.getUcConfig().getNoDataThresholdInMs()+" in ms) since we last got data on this connection ("+config.getId()+"), stop sending pings");
                stopAgent();
            }
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

        synchronized (agentUpdateStateLock) {

            if ( AgentState.STARTED.equals(state) || AgentState.RUNNING.equals(state) ){
                return;
            }

            recordTransmitter.setup(config);
            StatusWriter statusWriter = getStatusWriter(config);
            statusWriter.init();
            this.recordTransmitter.setStatusWriter(statusWriter);

            log.debug("Agent in thread id "+Thread.currentThread().getId()+" name "+Thread.currentThread().getName()+" agent obj "+this+" ready to create connection to snif " + config.getId());
            connThread = new Thread(recordTransmitter);
            connThread.setName(config.getId() + "-recordTransmitter-parent-"+Thread.currentThread().getId()+"_"+Thread.currentThread().getName());
            connThread.start();
            log.info("waiting for connection to snif to open" + config.getId());
            int retries = CONNECTION_RETRIES;
            while (!recordTransmitter.isStatusOpen() && retries > 0) {
                Thread.sleep(1000);
                retries--;
            }
            if (retries == 0 && !recordTransmitter.isStatusOpen()) {
                throw new Exception("Failed to connecto to sniffer " + config);
            }
            log.debug("Starting stats timer." + config.getId());
            timer = new Timer();
            timer.scheduleAtFixedRate(new StatsTimer(), 0, 1000);

            this.agentStatusGenerator = new AgentStatusGenerator(CONSUMER_Q_SIZE);
            state = AgentState.STARTED;
        }
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
        incIncomingRecordsCount();
        if (isStopStart) {
            log.trace("Agent is stop/start, and not accepting records "+config.getId());
            return;
        }
        try {
            waitForMessageQueue();
            messageQueue.put(new QueuedMessage(msg));
            transmitterStatsCollector.getCurrent().setLastMsgCreateTime(System.currentTimeMillis());
            if (log.isDebugEnabled()) {log.debug("Agent queue size ( proto message was added ): " + messageQueue.size()+" "+config.getId());}
        } catch (Exception e) {
            log.error(e);
        }
    }

    public void stopAgent() {
        synchronized (agentUpdateStateLock) {
            stop();
            state = AgentState.STOPPED;
        }
    }

    private void stop() {
        try {
            stopTimer();
            stopConnection();
            log.debug("Agent:" + config.getId() + " stopped");
            System.out.println("Agent:" + config.getId() + " stopped");
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

    public synchronized void incIncomingRecordsCount(){
        transmitterStatsCollector.getCurrent().incrementIncomingRecords();
    }

}
