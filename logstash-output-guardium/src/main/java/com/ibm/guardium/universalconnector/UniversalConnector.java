package com.ibm.guardium.universalconnector;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.ibm.guardium.proto.datasource.*;
import com.ibm.guardium.universalconnector.common.*;
import com.ibm.guardium.universalconnector.config.SnifferConfig;
import com.ibm.guardium.universalconnector.config.UCConfig;
import com.ibm.guardium.universalconnector.dispatcher.RecordDispatcher;
import com.ibm.guardium.universalconnector.exceptions.GuardUCCmdlineArgsException;
import com.ibm.guardium.universalconnector.exceptions.GuardUCException;
import com.ibm.guardium.universalconnector.transformer.JsonRecordTransformer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UniversalConnector {
    private static Log log = LogFactory.getLog(UniversalConnector.class);

    private boolean shouldReadFromFile = false;
    private String fileName;
    private boolean shouldWaitForMain = true;
    private volatile boolean running = true;
    private String version = "1.2.44";
    private String udsAgentVersion = "UniversalConnector (V" + version + ")";
    private UCConfig ucConfig = null;
    private RecordDispatcher recordDispatcher = null;
    private JsonRecordTransformer transformer = new JsonRecordTransformer();

    public UniversalConnector(){
        initConnector();
        log.info("UniversalConnector was initialized");

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
            log.info("Message to be dispatched to agent " + record);
            getRecordDispatcher().dispatch(transformer.transform(record));
            log.info("Message was dispatched to agent");
        } catch (Exception e){
            log.error("Failed to sendRecord from universal connector", e);
            throw new GuardUCException("Failed to sendRecord from universal connector", e);
        }
    }

    public RecordDispatcher getRecordDispatcher() throws Exception{
        if (recordDispatcher==null){
            synchronized (UniversalConnector.class){
                if (recordDispatcher!=null){
                    return recordDispatcher;
                }
                ConfigurationFetcherFactory factory = new ConfigurationFetcherFactory();
                ConfigurationFetcher configurationFetcher = factory.Build(ucConfig);
                List<SnifferConfig> snifferConfigs = configurationFetcher.fetch();
                recordDispatcher = new RecordDispatcher(ucConfig, snifferConfigs);
            }
        }
        return recordDispatcher;
    }

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
                getRecordDispatcher().dispatch(messages);
                getRecordDispatcher().dispatch(messages);
                getRecordDispatcher().dispatch(messages);
                    //agent.incIncomingRecordsCount();
                    //agent.send(message);
                    //agent.send(message.toByteArray());
                if (lineNumber % 100 == 0) {
                    progressPercentage(lineNumber, numeberOfLinesInFile);
                }
            }
            // need to wait for connection to send...
            try {
                getRecordDispatcher().waitForAllQToEmpty();
            } catch (InterruptedException e) {
                log.warn("Interrupted while waiting for messages to be sent.");
            }
            progressPercentage(numeberOfLinesInFile, numeberOfLinesInFile);
            getRecordDispatcher().stopAllAgents();
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

    private void initConnector(){
        log.info("using etc path as: " + Environment.UDS_ETC);
        JsonReader reader = null;
        try {
            PropertyConfigurator.configureAndWatch(Environment.UDS_ETC + "/log4j.properties", 10000);
            reader = new JsonReader(new FileReader(Environment.UDS_ETC + "/UniversalConnector.json"));
        } catch (FileNotFoundException e){
            log.error("Failed to find file");
            throw new IllegalArgumentException("Filed to find files during connector initialization, Path base is "+Environment.UDS_ETC, e);
        }
        UCConfig ucConfig = new Gson().fromJson(reader, UCConfig.class);
        ucConfig.setVersion(udsAgentVersion);
        this.ucConfig = ucConfig;
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

    public void onExit(){
        try {
            getRecordDispatcher().waitForAllQToEmpty();
        } catch (InterruptedException e) {
            log.warn("Interrupted while waiting for messages to be sent.");
        } catch (Exception e){
            log.error("Error on waiting for messages to be sent");
        }
        try {
            getRecordDispatcher().stopAllAgents();
        } catch (Exception e){
            log.error("Error on stopping agents",e);
        }
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

