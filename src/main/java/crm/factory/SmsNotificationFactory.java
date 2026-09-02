package crm.factory;

import crm.model.entity.Contact;
import crm.model.entity.Enrollment;

/**
 * Factory pentru notificări SMS (mai concise).
 */
public class SmsNotificationFactory implements NotificationFactory {

    private static final String CHANNEL = "SMS";

    @Override
    public Notification createWelcomeNotification(Contact contact) {
        String body = "Salut! Bine ai venit la Training IT! Un consultant te va contacta in curand.";
        return new Notification(contact.getPhone(), null, body, CHANNEL);
    }

    @Override
    public Notification createEnrollmentConfirmation(Contact contact, Enrollment enrollment) {
        String body = "Inscrierea ta a fost confirmata. Multumim!";
        return new Notification(contact.getPhone(), null, body, CHANNEL);
    }
}
