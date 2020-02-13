package com.ibm.guardium.universalconnector.dispatcher;

import com.google.protobuf.Message;
import com.ibm.guardium.proto.datasource.*;
import com.ibm.guardium.universalconnector.UniversalConnector;
import com.ibm.guardium.universalconnector.agent.Agent;
import com.ibm.guardium.universalconnector.config.SnifferConfig;
import com.ibm.guardium.universalconnector.config.UCConfig;
import com.ibm.guardium.universalconnector.exceptions.GuardUCException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RecordDispatcher {
    private static Log log = LogFactory.getLog(UniversalConnector.class);

    private UCConfig ucConfig;
    private List<SnifferConfig> snifferConfigs;
    private List<Agent> allAgents = new ArrayList<>();
    private final AtomicInteger atomicInteger = new AtomicInteger(-1);

    public RecordDispatcher(UCConfig ucConfig, List<SnifferConfig> snifferConfigs) {
        this.ucConfig = ucConfig;
        this.snifferConfigs = snifferConfigs;
        initAgents();
    }

//    /*
//    * As currently no decision is made regarding loadbalancing/failover/etc
//    * just use first available sniffer for sending the data.
//    * */
//    public void dispatch(String record){
//        int currentIndex;
//        int nextIndex;
//        do {
//            currentIndex = atomicInteger.get();
//            nextIndex = currentIndex< Integer.MAX_VALUE ? currentIndex+1: 0;
//        } while (!atomicInteger.compareAndSet(currentIndex, nextIndex));
//
//        int nextAgentIndex = nextIndex % allAgents.size();
//        Agent agent = allAgents.get(nextAgentIndex);
//        agent.incIncomingRecordsCount();
//        agent.send(record.getBytes());
//    }
//

    public void dispatch(String record){
        Agent agent = getNextAgent();
        agent.incIncomingRecordsCount();
        agent.send(record.getBytes());
    }

    public void dispatch(Message msg){
        Agent agent = getNextAgent();
        agent.incIncomingRecordsCount();
        agent.send(msg);
    }
    /*
    * Make sure all messages go via same agent
    * */
    public void dispatch(List<Datasource.Guard_ds_message> messages){
        Agent agent = getNextAgent();
        for (Datasource.Guard_ds_message message : messages) {
            try{
                agent.incIncomingRecordsCount();
                agent.send(message);
            } catch (Exception e){
                log.error("Error sending message via agent "+agent.getSnifferConfig(), e);
            }
        }
    }

    public void waitForAllQToEmpty(){
        for (Agent agent : allAgents) { //todo: handle threads
            try {
                agent.waitForQToEmpty();
            } catch (InterruptedException e){
                log.warn("Interrupted while waiting for messages to be sent agent "+agent.getSnifferConfig());
            }
        }
    }

    public void stopAllAgents(){
        for (Agent agent : allAgents) { //todo: handle threads
            try {
                agent.stop();
            } catch (Exception e){
                log.warn("Error while stopping agent "+agent.getSnifferConfig());
            }
        }
    }

    public Agent getNextAgent(){
        int currentIndex;
        int nextIndex;
        do {
            currentIndex = atomicInteger.get();
            nextIndex = currentIndex< Integer.MAX_VALUE ? currentIndex+1: 0;
        } while (!atomicInteger.compareAndSet(currentIndex, nextIndex));

        int nextAgentIndex = nextIndex % allAgents.size();
        log.info("Next agent index is "+nextAgentIndex);
        return allAgents.get(nextAgentIndex);
    }

    private void initAgents(){
        for (SnifferConfig snifferConfig : snifferConfigs) {
            try {
                Agent agent = new Agent(ucConfig, snifferConfig);
                agent.start();
                allAgents.add(agent);
            } catch (Exception e){
                log.error("Failed to creating/starting agent for sniffer configuration "+snifferConfig, e);
                System.err.print(e);
            }
        }
        if (allAgents.size()==0){
            log.error("No agent is available");
            throw new GuardUCException("No agent is available based on current settings");
        }
    }


}
