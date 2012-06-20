package net.intelie.monitor.engine;

import com.google.common.io.Files;
import net.intelie.monitor.events.BaseEvent;
import net.intelie.monitor.events.UnhandledEvent;
import net.intelie.monitor.listeners.Listener;
import org.apache.log4j.Logger;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 */
public class Collector {

    private static Logger logger = Logger.getLogger(Collector.class);

    private final EngineChecker checker;
    private final Listener listener;
    private final QueueCollection collection;
    private final File statusFile;
    private final Timer timer;

    private static final Integer INTERVAL_IN_SECS = 20;


    public Collector(EngineChecker checker, Listener listener, QueueCollection collection, File statusFile) {
        this.checker = checker;
        this.listener = listener;
        this.collection = collection;
        this.statusFile = statusFile;

        timer = new Timer();
    }

    public void start() {
        timer.schedule(new MonitorTask(), 0, INTERVAL_IN_SECS * 1000);
    }

    public void checkAll() {
        logger.debug("Retrieving information");
        try {
            try {
                checker.connect();
                collection.checkAll(checker);
                append("OK - all queues are fine");
            } catch (BaseEvent e) {
                append("CRITICAL - " + e.getMessage());
                listener.notify(e);
            } catch (Exception e) {
                append("CRITICAL - " + e.getMessage());
                listener.notify(new UnhandledEvent(e));
                logger.warn("Error retrieving information: " + e.getMessage());
                logger.debug(e);
            } finally {
                checker.disconnect();
            }
        } catch (Exception e) {
            logger.warn("Error notifying: " + e.getMessage());
            logger.debug(e.getStackTrace());
        }
    }

    private class MonitorTask extends TimerTask {
        public void run() {
            checkAll();
        }


    }

    private void append(String msg) {
        if (statusFile == null) return;
        System.out.println(msg);
        try {
            Files.append(msg + "\n", statusFile, Charset.defaultCharset());
        } catch (Exception e) {
            logger.warn("Exception trying to append status to file " + statusFile, e);
        }
    }


}

