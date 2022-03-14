package com.ibm.guardium.universalconnector;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.ibm.guardium.proto.datasource.*;
import com.ibm.guardium.universalconnector.commons.*;
import com.ibm.guardium.universalconnector.common.*;
import com.ibm.guardium.universalconnector.config.SnifferConfig;
import com.ibm.guardium.universalconnector.config.UCConfig;
import com.ibm.guardium.universalconnector.dispatcher.RecordDispatcher;
import com.ibm.guardium.universalconnector.exceptions.GuardUCCmdlineArgsException;
import com.ibm.guardium.universalconnector.exceptions.GuardUCException;
import com.ibm.guardium.universalconnector.transformer.JsonRecordTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UniversalConnector {
    private static Logger log = LogManager.getLogger(UniversalConnector.class);
    public static final String PERSISTENT_CONFIGURATION_FILE_NAME = "PersistentConfig.json";

    private boolean shouldReadFromFile = false;
    private String fileName;
    private boolean shouldWaitForMain = true;
    private volatile boolean running = true;
    private String version = "1.2.44";
    private String udsAgentVersion = "(V" + version + ")";
    private final UCConfig ucConfig;
    private final RecordDispatcher recordDispatcher;
    private final JsonRecordTransformer transformer;

    public UniversalConnector(){
        log.info("using etc path as: " + Environment.getUcEtc());
        transformer = new JsonRecordTransformer();
        try {
            JsonReader reader = new JsonReader(new FileReader(Environment.getUcEtc() + "UniversalConnector.json"));
            UCConfig config = new Gson().fromJson(reader, UCConfig.class);
            config.setVersion(udsAgentVersion);
            this.ucConfig = config;
        } catch (FileNotFoundException e){
            log.error("Failed to find file "+Environment.getUcEtc() + "UniversalConnector.json");
            throw new IllegalArgumentException("Failed to find files during connector initialization, Path base is "+Environment.getUcEtc(), e);
        }

        try {
            ConfigurationFetcherFactory factory = new ConfigurationFetcherFactory();
            ConfigurationFetcher configurationFetcher = factory.Build(ucConfig);
            List<SnifferConfig> snifferConfigs = configurationFetcher.fetch();
            recordDispatcher = new RecordDispatcher(ucConfig, snifferConfigs);
        } catch (Exception e) {
            log.error("Failed to initialize record dispatcher", e);
            throw  new IllegalArgumentException("Failed initialize record dispatcher", e);
        }

        log.info("UniversalConnector was initialized");
        log.debug("The Thread name is " + Thread.currentThread().getId() + "__" +Thread.currentThread().getName()+" this "+this);

    }

    private void parseCommandLineArgs(String[] args){
        final Map<String, List<String>> params = new HashMap<>();

        List<String> options = null;
        for (String a : args) {
            if (a.charAt(0) == '-') {
                if (a.length() < 2) {
                    throw new GuardUCCmdlineArgsException("Error at argument " + a);
                }

                options = new ArrayList<>();
                params.put(a.substring(1), options);
            }
            else if (options != null) {
                options.add(a);
            }
            else {
                throw new GuardUCCmdlineArgsException("Illegal parameter usage");
            }
        }
        if ((null != params.get("v")) || (null != params.get("version"))){
            System.out.println(udsAgentVersion);
            System.exit(1);
        }
        if ((null != params.get("h")) || (null != params.get("help"))){
            String helpMgs = udsAgentVersion + ": \n" +
                    "Options:\n" +
                    "   -h, -help            (will show this message)\n" +
                    "   -v, -version         (will show version info)\n" +
                    "   -read <filename>     (will read records from file)\n";
            System.out.println(helpMgs);
            System.exit(1);
        }
        if ((null != params.get("read")) && (null != params.get("write"))){
            throw new GuardUCCmdlineArgsException("options -read and -write can not be combined.");
        }
        options = params.get("read");
        if (options != null){
            if (options.size() != 1) {
                throw new GuardUCCmdlineArgsException("missing file name");
            }
            fileName = options.get(0);
            shouldReadFromFile = true;
        }
    }

    private static void progressPercentage(float done, float total) {
        if (done > total) {
            throw new IllegalArgumentException();
        }
        float maxBarSize = 100; // 10unit for 100%
        int remainPct = (int)(done / total * maxBarSize) ;
        char defaultChar = '.';
        String icon = "=";
        String bar = new String(new char[(int)maxBarSize]).replace('\0', defaultChar) + "]";
        StringBuilder barDone = new StringBuilder();
        barDone.append("[");
        for (int i = 0; i < remainPct; i++) {
            barDone.append(icon);
        }
        String barRemain = bar.substring(remainPct, bar.length());
        log.debug("\r" + barDone + barRemain + " " + remainPct + "%");
        if (done == total) {
            //System.out.print("\n");
            log.debug("\n");
        }
    }

    private int getNumberOfLinesInFile(String fileName)  throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            int lines = 0;
            while (reader.readLine() != null) lines++;
            return lines;
        }
    }

    public void sendRecord(String record)throws GuardUCException {
        try {
            //getAgentInstance().send(record.getBytes());
            if (log.isDebugEnabled()) { log.debug("Message to be dispatched to agent " + record); }

            recordDispatcher.dispatch(transformer.transform(record));

            if (log.isDebugEnabled()) { log.debug("Message was dispatched to agent");}

        } catch (Exception e){
            log.error("Failed to sendRecord from universal connector", e);
            throw new GuardUCException("Failed to sendRecord from universal connector", e);
        }
    }

    public void onExit(){
        log.debug("UniversalConnector on exit");
        try {
            log.debug("UniversalConnector onExit waitForAllQToEmpty");
            recordDispatcher.waitForAllQToEmpty();
        } catch (Exception e){
            log.error("Error on waiting for messages to be sent onExit (emptying messages queue). The Thread name is " + Thread.currentThread().getId() + "__" +Thread.currentThread().getName()+" this "+this);
        }
        try {
            log.debug("UniversalConnector onExit stopAllAgents, dispatcher is "+recordDispatcher+", this is "+this);
            recordDispatcher.stopAllAgents();
        } catch (Exception e){
            log.error("Error on stopping agents",e);
        }
    }

    //---- section below only meant to be used when using ucconnector locally, not us a plugin, and has not be tested for a while -----------//
    private void readFromFileToGuard(String recordsFileName) throws Exception{
        log.info("Reading and sending to Guard from file:" + recordsFileName);

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(recordsFileName)));) {
            int lineNumber = 0;
            int numeberOfLinesInFile = getNumberOfLinesInFile(recordsFileName);
            String line;
            log.debug("Sending records");
            while ((line = bufferedReader.readLine()) != null) {
                lineNumber++;
                List<com.ibm.guardium.proto.datasource.Datasource.Guard_ds_message> messages = transformer.transform(line);
                recordDispatcher.dispatch(messages);
                recordDispatcher.dispatch(messages);
                recordDispatcher.dispatch(messages);
                    //agent.incIncomingRecordsCount();
                    //agent.send(message);
                    //agent.send(message.toByteArray());
                if (lineNumber % 100 == 0) {
                    progressPercentage(lineNumber, numeberOfLinesInFile);
                }
            }
            // need to wait for connection to send...
            try {
                recordDispatcher.waitForAllQToEmpty();
            } catch (Exception e) {
                log.warn("Interrupted while waiting for messages to be sent. The Thread name is " + Thread.currentThread().getId() + "__" +Thread.currentThread().getName()+" this "+this);
            }
            progressPercentage(numeberOfLinesInFile, numeberOfLinesInFile);
            recordDispatcher.stopAllAgents();
            log.debug("Done sending.");
        } catch (IOException e) {
            log.error(e);
        }
    }

    private void addShutdownHook(){
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    log.debug("Shutting down the connector.");
                    running = false;
                    while(shouldWaitForMain) Thread.sleep(100);
                } catch (Exception e) {
                    log.error("Signal handler failed, reason "+e);
                }
            }
        });
    }

    private void signalShutdownHookToExit(){
        shouldWaitForMain = false;
    }


    private void connectorMain(String[] args) throws Exception {

        parseCommandLineArgs(args);

        log.info("Agent about to be started.");

        if (shouldReadFromFile) {
            log.info("Going to send Guardium data from file "+fileName);
            readFromFileToGuard(fileName);
            log.info("Finished to send Guardium data from file "+fileName);
            System.exit(0);
        }

        try {
            addShutdownHook();
            log.info("Going to create Agent instance.");
//            agentInstance = getAgentInstance();
            // handle state!!! - may be stopped/etc
            log.info("Agent started.");
            while(running) Thread.sleep(1000);
        } catch (Exception e) {
            log.error("will need to stop agent", e);
        }
        signalShutdownHookToExit();
        log.info("UniversalConnector stopped.");
    }

    public static void main(String[] args) {
        try {
            UniversalConnector c = new UniversalConnector();
            c.connectorMain(args);
            LogManager.shutdown();
        }catch (Exception e){
            log.error("UniversalConnector main exited with exception", e);
            e.printStackTrace();
        }
    }
}

