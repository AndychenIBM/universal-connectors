package com.ibm.guardium.universalconnector.dispatcher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.ibm.guardium.proto.datasource.*;
import com.ibm.guardium.universalconnector.UniversalConnector;
import com.ibm.guardium.universalconnector.agent.Agent;
import com.ibm.guardium.universalconnector.common.ConfigurationFetcher;
import com.ibm.guardium.universalconnector.common.ConfigurationFetcherFactory;
import com.ibm.guardium.universalconnector.common.Environment;
import com.ibm.guardium.universalconnector.config.*;
import com.ibm.guardium.universalconnector.exceptions.GuardUCException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
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
    private static Object persistency_flag = new Object();

    public RecordDispatcher(UCConfig ucConfig, List<SnifferConfig> snifferConfigs) {
        this.ucConfig = ucConfig;
        this.snifferConfigs = snifferConfigs;
        try {
            initPersistedConfigurations();
        } catch (Exception e){
            log.error("Failed to load persisted configuration, agent list will be empty on start", e);
        }

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
            ConnectionConfig cc = new ConnectionConfig(ucConfig, snifferConfigs.get(0), dbDetails);
            addAgentToMap(cc);
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

    public void persistAgentConfigurations() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        log.debug("persistAgentConfigurations start");
        synchronized (persistency_flag) {
            List<DatabaseDetails> dbs = agentsMap==null ? Collections.EMPTY_LIST : agentsMap.values().stream().map(a -> a.getConfig().getDatabaseDetails()).collect(Collectors.toList());
            try(FileWriter fw = new FileWriter(Environment.getPersistentConfigurationPath())) {
                PersistentConfig persistentConfig = new PersistentConfig();
                persistentConfig.setSnifferConfig(snifferConfigs.get(0));
                persistentConfig.setUcConfig(ucConfig);
                persistentConfig.setDbs(dbs);

                log.debug("persistAgentConfigurations path is " + Environment.getPersistentConfigurationPath());

                gson.toJson(persistentConfig, fw);
                fw.flush();
                fw.close();
                log.debug("persistAgentConfigurations finished saving to external folder");
            } catch (Throwable t) {
                log.error("Failed to persist configruation", t);
            }
        }
    }


    public void initPersistedConfigurations(){
        PersistentConfig config = null;
        synchronized (persistency_flag) {
            try (FileReader fileReader = new FileReader( Environment.getPersistentConfigurationPath())){
                JsonReader reader = new JsonReader(fileReader);
                config = new Gson().fromJson(reader, PersistentConfig.class);
                log.debug("Finished loading connections configuration from file, config is "+new Gson().toJson(config));
            } catch (FileNotFoundException e){
                log.info("Persisted configurations file "+Environment.getPersistentConfigurationPath()+" was not found, perhaps first time logstash loads, agents map will be empty on initialization");
                return;
            } catch (Exception e){
                log.error("Failed to deserializeAgentConfigurations from file "+Environment.getPersistentConfigurationPath()+", agents map will be empty on initialization ", e);
                return;
            }
        }

        UCConfig persistedUcConfig = config.getUcConfig();
        SnifferConfig perSnifferConfig = config.getSnifferConfig();
        Collection<DatabaseDetails> dbs = config.getDbs();
        for (DatabaseDetails db : dbs) {
            ConnectionConfig cc = new ConnectionConfig(persistedUcConfig, perSnifferConfig, db);
            validatePersistedConnectionConfig(cc);
            addAgentToMap(cc);
        }
    }


    private void validateConnectionsLimit(){
        if (ucConfig.getSnifferConnectionsLimit() != null && ucConfig.getSnifferConnectionsLimit() < agentsMap.size()) {
            log.error("Limit of existing connections to guardium has exceeded, limit is " + ucConfig.getSnifferConnectionsLimit());
            throw new GuardUCException("Exceeded number of connections to guardium");
        }
    }

    /**
     * When persistent uc might have sent data to ManagedUnit1 and after restart it was configured to send data to ManagedUnit2
     * need to
     * 1. Inform ManagedUnit1 to remove connection
     * 2. Update connection details to send data to ManagedUnit2
     *
     *
     * @param cc
     */
    private void validatePersistedConnectionConfig(ConnectionConfig cc){
        boolean configHasChanged = !snifferConfigs.get(0).equals(cc.getSnifferConfig()) || !ucConfig.equals(cc.getUcConfig());
        if (configHasChanged) {
            // 1. remove old stap details from managed unit
            // todo nataly

            // 2. update cc to current settings
            cc.setSnifferConfig(snifferConfigs.get(0));
            cc.setUcConfig(ucConfig);
        }
    }

    private void addAgentToMap(ConnectionConfig cc){

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

                    // since on container restart we may not get logstash going down event (on which normally configuratin should be persisted ) -
                    // persist configuration every time agent is added to the map
                    // in case agent is removed - will need update saved file.
                    persistAgentConfigurations();

                } catch (Exception e) {
                    log.error("Failed to creating/starting agent for connection configuration " + cc, e);
                }
            }
        }
    }
}
