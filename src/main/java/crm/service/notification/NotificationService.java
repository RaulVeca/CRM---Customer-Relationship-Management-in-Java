package crm.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import crm.factory.EmailNotificationFactory;
import crm.factory.NotificationFactory;
import crm.factory.NotificationFactory.Notification;
import crm.factory.SmsNotificationFactory;
import crm.model.entity.Contact;
import crm.model.entity.Enrollment;

/**
 * SINGLETON PATTERN - NotificationService
 * 
 * Folosește FACTORY PATTERN intern pentru a crea notificări
 * de diferite tipuri (email, SMS).
 * 
 * În producție, această clasă ar trimite efectiv emailuri
 * prin SMTP și SMS prin Twilio/similar.
 * În acest exemplu doar logăm.
 */
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private static volatile NotificationService instance;

    private final NotificationFactory emailFactory;
    private final NotificationFactory smsFactory;

    private NotificationService() {
        this.emailFactory = new EmailNotificationFactory();
        this.smsFactory = new SmsNotificationFactory();
    }

    public static NotificationService getInstance() {
        if (instance == null) {
            synchronized (NotificationService.class) {
                if (instance == null) {
                    instance = new NotificationService();
                }
            }
        }
        return instance;
    }

    public void sendWelcomeEmail(Contact contact) {
        if (contact == null || contact.getEmail() == null) return;
        Notification n = emailFactory.createWelcomeNotification(contact);
        send(n);
    }

    public void sendEnrollmentConfirmation(Contact contact, Enrollment enrollment) {
        Notification email = emailFactory.createEnrollmentConfirmation(contact, enrollment);
        send(email);

        if (contact.getPhone() != null) {
            Notification sms = smsFactory.createEnrollmentConfirmation(contact, enrollment);
            send(sms);
        }
    }

    public void sendSessionReminder(Contact contact, String sessionDetails) {
        Notification email = emailFactory.createSessionReminder(contact, sessionDetails);
        send(email);

        if (contact.getPhone() != null) {
            Notification sms = smsFactory.createSessionReminder(contact, sessionDetails);
            send(sms);
        }
    }

    public void sendFollowUp(Contact contact, String reason) {
        Notification n = emailFactory.createFollowUp(contact, reason);
        send(n);
    }

    /**
     * Trimite efectiv notificarea. În implementarea reală ar folosi SMTP/Twilio.
     */
    private void send(Notification n) {
        logger.info("[{}] Către: {}, Subiect: {}", 
            n.channel, n.recipient, 
            n.subject != null ? n.subject : "(fără subiect)");
        logger.debug("Conținut:\n{}", n.body);

        // TODO: integrare reală cu SMTP/Twilio
    }
}
