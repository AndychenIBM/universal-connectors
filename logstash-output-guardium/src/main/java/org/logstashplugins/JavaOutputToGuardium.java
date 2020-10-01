package org.logstashplugins;

import co.elastic.logstash.api.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ibm.guardium.universalconnector.UniversalConnector;
import com.ibm.guardium.universalconnector.common.Environment;

import com.ibm.guardium.universalconnector.common.GuardConstants;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.CountDownLatch;


// class name must match plugin name
@LogstashPlugin(name = "java_output_to_guardium")
public class JavaOutputToGuardium implements Output {

    static {
        try {
            LoggerContext context = (LoggerContext) LogManager.getContext(false);
            File file = new File(Environment.getLog42Conf());
            context.setConfigLocation(file.toURI());
        } catch (Exception e){
            System.err.println("Failed to load log4j configuration "+e.getMessage());
            e.printStackTrace();
        }
    }
    public static final PluginConfigSpec<String> PREFIX_CONFIG =
            PluginConfigSpec.stringSetting("prefix", "");

    private static Logger log = LogManager.getLogger(JavaOutputToGuardium.class);

    private final String id;
    private String prefix;
    private PrintStream printer;
    private final CountDownLatch done = new CountDownLatch(1);
    private volatile boolean stopped = false;
    private static UniversalConnector connector = null;//new UniversalConnector();
    private static Gson gson = new Gson();

    // all plugins must provide a constructor that accepts id, Configuration, and Context
    public JavaOutputToGuardium(final String id, final Configuration configuration, final Context context) {
        this(id, configuration, context, System.out);
    }

    JavaOutputToGuardium(final String id, final Configuration config, final Context context, OutputStream targetStream) {
        // constructors should validate configuration options
        this.id = id;
        prefix = config.get(PREFIX_CONFIG);
        printer = new PrintStream(targetStream);
        connector = new UniversalConnector();
        log.info("Finished JavaOutputToGuardium constructor");
    }

    @Override
    public void output(final Collection<Event> events) {
        Iterator<Event> z = events.iterator();
        Event event;
        while (z.hasNext() && !stopped) {
            event = z.next();
            try {

                if (event.getField(GuardConstants.GUARDIUM_RECORD_FIELD_NAME)!=null) {

                    if (log.isDebugEnabled()) {
                        log.debug("==========Event " + logEvent(event));
                    }

                    String recordString = event.getField("GuardRecord").toString();

                    if (log.isDebugEnabled()) {
                        log.debug("==========Record " + recordString);
                    }

                    connector.sendRecord(recordString);

                }
                /*else {
                    log.warn("No record was found in event, please check parser logs");
                }*/

            } catch (Exception ex){
                log.error("Failed to handle event "+logEvent(event), ex);
            }
            if (log.isDebugEnabled()) {log.debug("==========Finished one event, total events size is " + events.size());}
        }
    }

    @Override
    public void stop() {
        stopped = true;
        done.countDown();
        log.info("in stop");
        connector.onExit();
        log.info("in stop after connector.onExit()");
    }

    @Override
    public void awaitStop() throws InterruptedException {
        done.await();
    }

    @Override
    public Collection<PluginConfigSpec<?>> configSchema() {
        // should return a list of all configuration options for this plugin
        return Collections.singletonList(PREFIX_CONFIG);
    }

    @Override
    public String getId() {
        return id;
    }

    private static String logEvent(Event event){
        StringBuffer sb = new StringBuffer();
        sb.append("{ ");
        boolean first = true;
        for (Map.Entry<String, Object> stringObjectEntry : event.getData().entrySet()) {
            if (!first){
                sb.append(",");
            }
            sb.append("\""+stringObjectEntry.getKey()+"\" : \""+stringObjectEntry.getValue()+"\"");
            first = false;
        }
        sb.append(" }");
        return sb.toString();
    }

}
