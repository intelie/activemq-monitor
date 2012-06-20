package net.intelie.monitor.notifiers;

public interface Notifier {
    void send(String subject, String body);
}
