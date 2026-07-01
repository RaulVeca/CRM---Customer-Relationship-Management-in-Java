package crm.observer.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import crm.model.entity.MeditationSession;
import crm.observer.CrmEvent;
import crm.observer.events.SessionBookedEvent;
import crm.patterns.Observer;
import crm.service.invoice.InvoiceService;

/**
 * OBSERVER care generează automat o factură la fiecare rezervare de ședință (booking).
 *
 * <p>Ascultă {@code SESSION_BOOKED}. Eșecul generării nu trebuie să oprească
 * rezervarea, așa că excepțiile sunt doar logate.</p>
 */
public class InvoiceGenerationObserver implements Observer<CrmEvent> {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceGenerationObserver.class);

    @Override
    public void update(CrmEvent event) {
        if (!(event instanceof SessionBookedEvent)) return;

        MeditationSession session = ((SessionBookedEvent) event).getSession();
        if (session == null || session.getId() == null) return;

        try {
            InvoiceService.getInstance().generateForSession(session);
        } catch (Exception ex) {
            logger.error("Error auto-generating the invoice for session {}",
                    session.getId(), ex);
        }
    }
}
