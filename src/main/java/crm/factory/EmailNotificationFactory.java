package crm.factory;

import crm.model.entity.Contact;
import crm.model.entity.Enrollment;

/**
 * Factory pentru notificări de tip Email.
 */
public class EmailNotificationFactory implements NotificationFactory {

    private static final String CHANNEL = "EMAIL";

    @Override
    public Notification createWelcomeNotification(Contact contact) {
        String subject = "Bine ai venit la Training IT!";
        String body = "Salut " + contact.getFullName().orElse("") + ",\n\n" +
                "Thank you for your interest in our courses!\n\n" +
                "We are a company specialized in IT training and we offer courses " +
                "on Java, Python, AI and much more.\n\n" +
                "A consultant will contact you soon to discuss the right options.\n\n" +
                "Cu drag,\nEchipa Training IT";
        return new Notification(contact.getEmail(), subject, body, CHANNEL);
    }

    @Override
    public Notification createEnrollmentConfirmation(Contact contact, Enrollment enrollment) {
        String subject = "Course enrollment confirmation";
        String body = "Salut " + contact.getFullName().orElse("") + ",\n\n" +
                "Your enrollment has been confirmed successfully!\n" +
                "Status: " + enrollment.getStatus() + "\n\n" +
                "You will soon receive details about the course schedule and the required materials.\n\n" +
                "Mult succes!\nEchipa Training IT";
        return new Notification(contact.getEmail(), subject, body, CHANNEL);
    }

    @Override
    public Notification createSessionReminder(Contact contact, String sessionDetails) {
        String subject = "Reminder sesiune curs";
        String body = "Salut " + contact.getFullName().orElse("") + ",\n\n" +
                "This is a reminder for tomorrow's session:\n\n" +
                sessionDetails + "\n\n" +
                "Ne vedem cu drag!\nEchipa Training IT";
        return new Notification(contact.getEmail(), subject, body, CHANNEL);
    }

    @Override
    public Notification createFollowUp(Contact contact, String reason) {
        String subject = "Follow-up: " + reason;
        String body = "Salut " + contact.getFullName().orElse("") + ",\n\n" +
                "We're glad to be in touch again.\n" +
                reason + "\n\n" +
                "Please reply when you have a moment.\n\n" +
                "Thank you!\nThe Training IT Team";
        return new Notification(contact.getEmail(), subject, body, CHANNEL);
    }
}
