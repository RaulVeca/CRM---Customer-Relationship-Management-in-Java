package crm.factory;

import crm.model.entity.Contact;
import crm.model.entity.Enrollment;

/**
 * ABSTRACT FACTORY PATTERN - Notification Factory
 * 
 * Definește interfața pentru fabrici de notificări.
 * Permite crearea de notificări de diferite tipuri (email, SMS, push)
 * fără a expune detaliile implementării.
 */
public interface NotificationFactory {

    Notification createWelcomeNotification(Contact contact);

    Notification createEnrollmentConfirmation(Contact contact, Enrollment enrollment);

    Notification createSessionReminder(Contact contact, String sessionDetails);

    Notification createFollowUp(Contact contact, String reason);

    /**
     * Clasa internă pentru o notificare.
     */
    class Notification {
        public final String recipient;
        public final String subject;
        public final String body;
        public final String channel; // EMAIL, SMS, PUSH

        public Notification(String recipient, String subject, String body, String channel) {
            this.recipient = recipient;
            this.subject = subject;
            this.body = body;
            this.channel = channel;
        }
    }
}
