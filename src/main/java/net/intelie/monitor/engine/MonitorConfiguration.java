package net.intelie.monitor.engine;

import com.google.common.base.Strings;
import net.intelie.monitor.listeners.QueueMonitorListener;
import net.intelie.monitor.notifiers.EmailNotifier;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MonitorConfiguration {
    private static Logger logger = Logger.getLogger(MonitorConfiguration.class);
    private String[] recipients, monitoredQueues;
    private String server, port, path;
    private String domain, brokerName, company;
    private String statusFile;
    private long notificationInterval;

    public MonitorConfiguration(String resource) {
        this(MonitorConfiguration.class.getClassLoader().getResourceAsStream(resource));
    }

    public MonitorConfiguration(InputStream stream) {
        try {
            Properties properties = new Properties();
            properties.load(stream);
            server = properties.getProperty("server");
            port = properties.getProperty("port");
            path = properties.getProperty("connectorPath");

            domain = properties.getProperty("domain");
            brokerName = properties.getProperty("brokerName");

            notificationInterval = Long.parseLong(properties.getProperty("minInterval")) * 60 * 1000;

            company = properties.getProperty("company");

            recipients = safeSplit(properties.getProperty("recipients"));
            statusFile = properties.getProperty("statusFile");
            monitoredQueues = safeSplit(properties.getProperty("monitor"));

            trimAll(recipients);
            trimAll(monitoredQueues);
        } catch (IOException e) {
            throw new RuntimeException("Could not load properties. Is file activemq-monitor.properties in classpath?", e);
        }
    }

    private String[] safeSplit(String s) {
        if (Strings.isNullOrEmpty(s)) return new String[0];
        return s.split(",");
    }

    private void trimAll(String[] values) {
        for (int i =0; i<values.length; i++)
            if (values[i] != null)
                values[i] = values[i].trim();
    }

    public QueueMonitorListener createListener() {
        return new QueueMonitorListener(company, new EmailNotifier(recipients), notificationInterval);
    }

    public QueueCollection createQueueMonitors() {
        return new QueueCollection(monitoredQueues);
    }

    public File statusFile() {
        return statusFile != null ? new File(statusFile) : null;
    }

    public ActiveMQChecker createChecker() {
        return new ActiveMQChecker(server, port, path, domain, brokerName);
    }
}
