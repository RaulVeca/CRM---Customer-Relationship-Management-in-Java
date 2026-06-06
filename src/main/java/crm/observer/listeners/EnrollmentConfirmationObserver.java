package crm.observer.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import crm.model.entity.Contact;
import crm.model.entity.Enrollment;
import crm.observer.CrmEvent;
import crm.observer.events.EnrollmentCreatedEvent;
import crm.patterns.Observer;
import crm.repository.ContactRepository;
import crm.service.notification.NotificationService;

/**
 * Observer care trimite email de confirmare la înscriere.
 */
public class EnrollmentConfirmationObserver implements Observer<CrmEvent> {

    private static final Logger logger = LoggerFactory.getLogger(EnrollmentConfirmationObserver.class);

    @Override
    public void update(CrmEvent event) {
        if (!(event instanceof EnrollmentCreatedEvent)) return;

        Enrollment enrollment = ((EnrollmentCreatedEvent) event).getEnrollment();
        if (enrollment == null || enrollment.getContactId() == null) return;

        try {
            Contact contact = ContactRepository.getInstance().getById(enrollment.getContactId());
            NotificationService.getInstance().sendEnrollmentConfirmation(contact, enrollment);
            logger.info("Confirmare înscriere trimisă către contact {}", enrollment.getContactId());
        } catch (Exception ex) {
            logger.error("Eroare trimitere confirmare înscriere", ex);
        }
    }
}
