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
                "Îți mulțumim pentru interesul față de cursurile noastre!\n\n" +
                "Suntem o companie specializată în training IT și oferim cursuri " +
                "de Java, Python, AI și multe altele.\n\n" +
                "Un consultant te va contacta în curând pentru a discuta opțiunile potrivite.\n\n" +
                "Cu drag,\nEchipa Training IT";
        return new Notification(contact.getEmail(), subject, body, CHANNEL);
    }

    @Override
    public Notification createEnrollmentConfirmation(Contact contact, Enrollment enrollment) {
        String subject = "Confirmare înscriere curs";
        String body = "Salut " + contact.getFullName().orElse("") + ",\n\n" +
                "Înscrierea ta a fost confirmată cu succes!\n" +
                "Status: " + enrollment.getStatus() + "\n\n" +
                "Vei primi în curând detalii despre programul cursului și materialele necesare.\n\n" +
                "Mult succes!\nEchipa Training IT";
        return new Notification(contact.getEmail(), subject, body, CHANNEL);
    }

    @Override
    public Notification createSessionReminder(Contact contact, String sessionDetails) {
        String subject = "Reminder sesiune curs";
        String body = "Salut " + contact.getFullName().orElse("") + ",\n\n" +
                "Acesta este un reminder pentru sesiunea de mâine:\n\n" +
                sessionDetails + "\n\n" +
                "Ne vedem cu drag!\nEchipa Training IT";
        return new Notification(contact.getEmail(), subject, body, CHANNEL);
    }

    @Override
    public Notification createFollowUp(Contact contact, String reason) {
        String subject = "Follow-up: " + reason;
        String body = "Salut " + contact.getFullName().orElse("") + ",\n\n" +
                "Ne pare bine să te recontactăm.\n" +
                reason + "\n\n" +
                "Te rugăm să ne răspunzi când ai un moment.\n\n" +
                "Mulțumim!\nEchipa Training IT";
        return new Notification(contact.getEmail(), subject, body, CHANNEL);
    }
}
