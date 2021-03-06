package net.intelie.monitor.notifiers;

import org.apache.log4j.Logger;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Properties;

public class EmailNotifier implements Notifier {
    private static Logger logger = Logger.getLogger(EmailNotifier.class);

    private Session session;
    private String[] recipients;

    public EmailNotifier(String[] recipients) {
        this.recipients = recipients;
        try {
            final Properties properties = new Properties();
            properties.load(this.getClass().getClassLoader().getResourceAsStream("mail.properties"));

            session = Session.getDefaultInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    logger.info("Authenticating " + properties.getProperty("mail.auth.user"));
                    return new PasswordAuthentication(properties.getProperty("mail.auth.user"), properties.getProperty("mail.auth.password"));
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Could not load mail properties. Is file mail.properties in classpath?", e);
        }
    }

    @Override
    public void send(String subject, String body) {
        if (recipients.length == 0) return;

        logger.warn("Notifying recipients: ");
        for (String to : recipients)
            logger.warn(to);

        try {
            MimeMessage message = new MimeMessage(session);
            for (String to : recipients)
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            if (recipients.length > 0) {
                message.setSubject(subject);
                message.setText(body);
                Transport.send(message);
            }
        } catch (MessagingException e) {
            throw new RuntimeException("Could not send email. Verify correctness of file mail.properties", e);
        }
    }

}
