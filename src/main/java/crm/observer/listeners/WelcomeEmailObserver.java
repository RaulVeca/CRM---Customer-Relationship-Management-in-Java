package crm.observer.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import crm.model.entity.Contact;
import crm.observer.CrmEvent;
import crm.observer.events.ContactCreatedEvent;
import crm.patterns.Observer;
import crm.service.notification.NotificationService;

/**
 * Observer care trimite email de bun venit la creare contact nou.
 */
public class WelcomeEmailObserver implements Observer<CrmEvent> {

    private static final Logger logger = LoggerFactory.getLogger(WelcomeEmailObserver.class);

    @Override
    public void update(CrmEvent event) {
        if (!(event instanceof ContactCreatedEvent)) return;

        ContactCreatedEvent e = (ContactCreatedEvent) event;
        Contact contact = e.getContact();

        if (contact == null || contact.getEmail() == null) {
            logger.warn("Nu pot trimite welcome email: email lipsă");
            return;
        }

        try {
            NotificationService.getInstance().sendWelcomeEmail(contact);
            logger.info("Welcome email trimis către: {}", contact.getEmail());
        } catch (Exception ex) {
            logger.error("Eroare trimitere welcome email", ex);
        }
    }
}
