package org.logstashplugins;

import co.elastic.logstash.api.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ibm.guardium.universalconnector.UniversalConnector;
import com.ibm.guardium.universalconnector.UniversalConnector;
import com.ibm.guardium.universalconnector.exceptions.GuardUCException;
import com.ibm.guardium.universalconnector.transformer.jsonrecord.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.logstash.ackedqueue.io.MmapPageIOV2;

import java.io.OutputStream;
import java.io.PrintStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;


// class name must match plugin name
@LogstashPlugin(name = "java_output_to_guardium")
public class JavaOutputToGuardium implements Output {

    public static final PluginConfigSpec<String> PREFIX_CONFIG =
            PluginConfigSpec.stringSetting("prefix", "");

    private static Log log = LogFactory.getLog(JavaOutputToGuardium.class);

    private final String id;
    private String prefix;
    private PrintStream printer;
    private final CountDownLatch done = new CountDownLatch(1);
    private volatile boolean stopped = false;
    private static UniversalConnector connector = null;//new UniversalConnector();
    private static Gson gson = new Gson();
    private JsonFromEventBuilder jsonFromEventBuilder = new JsonFromEventBuilder();

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
                if (log.isDebugEnabled()) {
                    log.debug("==========Event " + logEvent(event));
                }

                if (event.getField("Record")!=null) {

                    String recordString = event.getField("Record").toString();

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
        System.out.println("in stop");
        connector.onExit();
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
