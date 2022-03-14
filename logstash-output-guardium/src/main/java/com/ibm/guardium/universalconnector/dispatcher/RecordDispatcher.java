package com.ibm.guardium.universalconnector.dispatcher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.ibm.guardium.proto.datasource.*;
import com.ibm.guardium.universalconnector.agent.Agent;
import com.ibm.guardium.universalconnector.common.Environment;
import com.ibm.guardium.universalconnector.config.*;
import com.ibm.guardium.universalconnector.exceptions.GuardUCException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class RecordDispatcher {
    private static Logger log = LogManager.getLogger(RecordDispatcher.class);
    private final UCConfig ucConfig;
    private final List<SnifferConfig> snifferConfigs;
    private final ConcurrentMap<String, Agent> agentsMap; // id -> Agent instance

    // session_start and client_request must be put in the queue together one after another in order for sniffer to process it correctly
    private final Object must_put_2_messages_per_record_flag = new Object();
    private final Object agent_map_on_agent_create_flag = new Object();

    public RecordDispatcher(UCConfig ucConfig, List<SnifferConfig> snifferConfigs) {
        this.ucConfig = ucConfig;
        this.snifferConfigs = snifferConfigs;
        agentsMap = new ConcurrentHashMap<>();
    }

    /*
    * Make sure all specific db messages go via same agent
    * */
    public void dispatch(List<Datasource.Guard_ds_message> messages){
        try{
            Agent agent = getAgent(messages);
            synchronized (must_put_2_messages_per_record_flag) {
                for (Datasource.Guard_ds_message message : messages) {
                    agent.send(message);
                }
            }
        } catch (Exception e){
            String dbId = getDbIdFromMessages(messages);
            log.error("Error sending message via agent "+dbId, e);
            throw new GuardUCException("Error sending message via agent "+dbId, e);
        }
    }

    private String getDbIdFromMessages(List<Datasource.Guard_ds_message> messages){
        DatabaseDetails dbDetails = getDbDetailsFromMessages(messages);
        return dbDetails.getId();
    }

    private DatabaseDetails getDbDetailsFromMessages(List<Datasource.Guard_ds_message> messages){
        Datasource.Session_start sessionStart = messages.get(0).getSessionStart();
        return DatabaseDetails.buildFromMessage(sessionStart);
    }

    private Agent getAgent(List<Datasource.Guard_ds_message> messages) throws Exception {
        String dbId = getDbIdFromMessages(messages);
        try {
            //log.debug("The Thread name is " + Thread.currentThread().getId() + "__" +Thread.currentThread().getName());
            if (agentsMap.get(dbId) == null) {
                DatabaseDetails dbDetails = getDbDetailsFromMessages(messages);
                ConnectionConfig cc = new ConnectionConfig(ucConfig, snifferConfigs.get(0), dbDetails);
                addAgentToMap(cc);
            }
            Agent agent = agentsMap.get(dbId);
            if (Agent.AgentState.STOPPED.equals(agent.getState())) { // agent already exists but was stopped because of connection errors or because there was not data for more then an hour
                if (log.isDebugEnabled()) {
                    log.debug("About to start agent for " + dbId);
                }
                agent.start();
                if (log.isDebugEnabled()) {
                    log.debug("Started agent for " + dbId);
                }
            }
            return agent;

        } catch (Exception e){
            log.error("Error getiing Agent "+dbId, e);
            throw e;
        }
    }

    /**
     * when we clean queues - it does not matter from which thread it is done,
     * other threads need to wait till queue is released so dispatcher can move on to other tasks
     */
    public synchronized void waitForAllQToEmpty(){
        for (Agent agent : agentsMap.values()) { //todo: handle threads
            try {
                agent.waitForQToEmpty();
            } catch (InterruptedException e){
                log.warn("Interrupted while waiting for messages to be sent agent "+agent.getId());
            }
        }
    }

    /**
     * when we stop agents - it does not matter from which thread it is done,
     * other threads need to wait till dispatcher finishes stopping agents before dispatcher can proceed
     */
    public synchronized void stopAllAgents(){
        for (Agent agent : agentsMap.values()) { //todo: handle threads
            try {
                agent.stopAgent();
            } catch (Exception e){
                log.warn("Error while stopping agent "+agent.getId());
            }
        }
    }

    private boolean isConnectionsLimitExceeded(){
        return ucConfig.getSnifferConnectionsLimit()!=null && (agentsMap.size() >= ucConfig.getSnifferConnectionsLimit());
    }

    private void validateConnectionsLimit() {
        if (isConnectionsLimitExceeded()) {
            log.warn("Limit of existing connections to guardium is reached, limit is " + ucConfig.getSnifferConnectionsLimit()+", going to remove some agent");
            //throw new GuardUCException("Exceeded number of connections to guardium");
            // go over existing agents, check if some can be released -
            // stopped agent will be removed from map
            for (String agentId : agentsMap.keySet()) {
                if (agentsMap.get(agentId).getState().equals(Agent.AgentState.STOPPED) || agentsMap.get(agentId).getState().equals(Agent.AgentState.ERROR)) {
                    agentsMap.remove(agentId);
                }
            }
        }

        if (isConnectionsLimitExceeded()) {
            log.error("Limit of existing connections to guardium has exceeded, limit is " + ucConfig.getSnifferConnectionsLimit());
            throw new GuardUCException("Exceeded number of connections to guardium");
        }
    }

    private void addAgentToMap(ConnectionConfig cc) throws Exception{

        String dbId = cc.getDatabaseDetails().getId();

        synchronized (agent_map_on_agent_create_flag) {

            if (agentsMap.get(dbId)==null) {

                if (log.isDebugEnabled()) { log.debug("AgentMap values are " + map2string(agentsMap)); }

                validateConnectionsLimit();

                try {
                    if (log.isDebugEnabled()) { log.debug("Creating agent for " + dbId); }

                    Agent agent = new Agent(cc);
                    agent.start();
                    agentsMap.put(dbId, agent);

                    if (log.isDebugEnabled()) { log.debug("Finished creating agent for " + dbId);  }

                } catch (Exception e) {
                    log.error("Failed to creating/starting agent "+dbId+" for connection configuration " + cc, e);
                    throw e;
                }
            }
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

}
