package net.intelie.monitor.listeners;

import net.intelie.monitor.events.Event;
import net.intelie.monitor.notifiers.Notifier;
import org.apache.log4j.Logger;

public class QueueMonitorListener implements Listener {

    private static Logger logger = Logger.getLogger(QueueMonitorListener.class);

    private String company;
    private Notifier notifier;
    private long interval;
    private long lastTimestamp;

    public QueueMonitorListener(String company, Notifier notifier, long interval) {
        interval *= 60 * 1000; //minutes
        this.company = company;
        this.notifier = notifier;
        this.interval = interval;
        this.lastTimestamp = -interval;
    }

    public void notify(Event event) {
        notify(event, System.currentTimeMillis());
    }

    public void notify(Event event, long timestamp) {
        if (event instanceof Throwable)
            logger.warn("Event " + event.getMessage() + " at " + timestamp, (Throwable) event);
        else
            logger.warn("Event " + event.getMessage() + " at " + timestamp);

        if (timestamp - lastTimestamp < interval) {
            logger.info("Discarding [" + (timestamp - lastTimestamp) + "]...");
            return;
        }
        lastTimestamp = timestamp;

        notifier.send("[ERRO][" + company + "] " + event.getMessage(), event.getDetail());
    }

}

