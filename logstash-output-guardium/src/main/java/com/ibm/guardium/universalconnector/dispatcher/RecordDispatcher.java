package com.ibm.guardium.universalconnector.dispatcher;

import com.ibm.guardium.proto.datasource.*;
import com.ibm.guardium.universalconnector.UniversalConnector;
import com.ibm.guardium.universalconnector.agent.Agent;
import com.ibm.guardium.universalconnector.config.ConnectionConfig;
import com.ibm.guardium.universalconnector.config.DatabaseDetails;
import com.ibm.guardium.universalconnector.config.SnifferConfig;
import com.ibm.guardium.universalconnector.config.UCConfig;
import com.ibm.guardium.universalconnector.exceptions.GuardUCException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class RecordDispatcher {
    private static Logger log = LogManager.getLogger(RecordDispatcher.class);

    private UCConfig ucConfig;
    private List<SnifferConfig> snifferConfigs;
    private ConcurrentMap<String, Agent> agentsMap = new ConcurrentHashMap<>(); // id -> Agent instance
    // session_start and client_request must be put in the queue together one after another in order for sniffer to process it correctly
    private static Object must_put_2_messages_per_record_flag = new Object();
    private static Object agent_map_on_agent_create_flag = new Object();

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

    /*
    * Make sure all specific db messages go via same agent
    * */
    public void dispatch(List<Datasource.Guard_ds_message> messages){
        Agent agent = getAgent(messages);
        try{
            synchronized (must_put_2_messages_per_record_flag) {
                for (Datasource.Guard_ds_message message : messages) {
                    agent.incIncomingRecordsCount();
                    agent.send(message);
                }
            }
        } catch (Exception e){
            log.error("Error sending message via agent "+agent.getId(), e);
            throw new GuardUCException("Error sending message via agent "+agent.getId(), e);
        }
    }
    public static String map2string(Map<?,?> map){
        try{
            if (map==null){
                return "null";
            }
            if (map.entrySet()==null || map.entrySet().isEmpty()){
                return "map is empty";
            }
            return map.entrySet()
                    .stream()
                    .map(entry -> entry.getKey() + " - " + entry.getValue())
                    .collect(Collectors.joining(", "));

        } catch (Exception e){
            log.error("Error translating map to strng");
            return "Falied to log map";
        }
    }
    private Agent getAgent(List<Datasource.Guard_ds_message> messages){
        Datasource.Session_start sessionStart = messages.get(0).getSessionStart();
        DatabaseDetails dbDetails = DatabaseDetails.buildFromMessage(sessionStart);
        String dbId = dbDetails.getId();

        //log.debug("The Thread name is " + Thread.currentThread().getId() + "__" +Thread.currentThread().getName());
        if (agentsMap.get(dbId)==null) {

            synchronized (agent_map_on_agent_create_flag) {

                if (agentsMap.get(dbId)==null) {

                    if (log.isDebugEnabled()) { log.debug("AgentMap values are " + map2string(agentsMap)); }

                    if (ucConfig.getSnifferConnectionsLimit() != null && ucConfig.getSnifferConnectionsLimit() < agentsMap.size()) {
                        log.error("Limit of existing connections to guardium has exceeded, limit is " + ucConfig.getSnifferConnectionsLimit());
                        throw new GuardUCException("Exceeded number of connections to guardium");
                    }
                    try {
                        if (log.isDebugEnabled()) { log.debug("Creating agent for " + dbId); }

                        ConnectionConfig cc = new ConnectionConfig(ucConfig, snifferConfigs.get(0), dbDetails);
                        Agent agent = new Agent(cc);
                        agent.start();
                        agentsMap.put(dbId, agent);

                        if (log.isDebugEnabled()) { log.debug("Finished creating agent for " + dbId);  }
                    } catch (Exception e) {
                        log.error("Failed to creating/starting agent for sniffer configuration " + snifferConfigs.get(0) + " and dbDetails " + dbDetails, e);
                        System.err.print(e);
                    }
                }
            }
        }
        return agentsMap.get(dbId);
    }

    public void waitForAllQToEmpty(){
        for (Agent agent : agentsMap.values()) { //todo: handle threads
            try {
                agent.waitForQToEmpty();
            } catch (InterruptedException e){
                log.warn("Interrupted while waiting for messages to be sent agent "+agent.getId());
            }
        }
    }

    public void stopAllAgents(){
        for (Agent agent : agentsMap.values()) { //todo: handle threads
            try {
                agent.stop();
            } catch (Exception e){
                log.warn("Error while stopping agent "+agent.getId());
            }
        }
    }

    private void initAgents(){
//        for (SnifferConfig snifferConfig : snifferConfigs) {
//            try {
//                ConnectionConfig cc = new ConnectionConfig(ucConfig, snifferConfig, null);
//                Agent agent = new Agent(cc);
//                agent.start();
//                allAgents.add(agent);
//            } catch (Exception e){
//                log.error("Failed to creating/starting agent for sniffer configuration "+snifferConfig, e);
//                System.err.print(e);
//            }
//        }
//        if (allAgents.size()==0){
//            log.error("No agent is available");
//            throw new GuardUCException("No agent is available based on current settings");
//        }
    }


}
