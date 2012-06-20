package net.intelie.monitor.engine;

import com.google.common.io.Files;
import net.intelie.monitor.events.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

/**
 * Gets information from ActiveMQ through JMX frequently and uses rules to determine if
 * is necessary to alert about some behaviour.
 */
public class QueueCollection {
    static Logger logger = Logger.getLogger(QueueCollection.class);

    private final List<QueueMonitor> monitors;
    private final String[] monitoredQueues;
    private final File statusFile;

    public QueueCollection(File statusFile, String[] monitoredQueues) {
        this.monitoredQueues = monitoredQueues;
        this.monitors = new LinkedList<QueueMonitor>();
        this.statusFile = statusFile;

        for (String queueName : monitoredQueues) {
            monitors.add(new QueueMonitor(queueName));
        }
    }

    public void checkAll(EngineChecker engineChecker) throws CompositeEvent {
        LinkedList<Event> events = new LinkedList<Event>();
        for (QueueMonitor monitor : monitors)
            try {
                monitor.check(engineChecker);
            } catch (QueueNotFound e) {
                events.add(e);
            } catch (QueueStoppedConsuming e) {
                events.add(e);
            } catch (Throwable e) {
                events.add(new UnhandledEvent(e));
            }

        if (!events.isEmpty()) {
            for (Event event : events)
                append("CRITICAL - " + event.getMessage());

            throw new CompositeEvent(events);
        } else {
            append("OK - queues " + StringUtils.join(monitoredQueues, ", "));
        }
    }

    private void append(String msg) {
        if (statusFile == null) return;
        try {
            Files.append(msg, statusFile, Charset.defaultCharset());
        } catch (Exception e) {
            logger.warn("Exception trying to append status to file " + statusFile, e);
        }
    }


}
