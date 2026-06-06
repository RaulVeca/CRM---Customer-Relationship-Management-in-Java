package crm.observer.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import crm.model.entity.Activity;
import crm.observer.CrmEvent;
import crm.observer.events.ActivityCompletedEvent;
import crm.patterns.Observer;
import crm.service.contact.ContactService;

/**
 * Observer care actualizează scorul lead-ului când o activitate e finalizată.
 */
public class LeadScoreUpdateObserver implements Observer<CrmEvent> {

    private static final Logger logger = LoggerFactory.getLogger(LeadScoreUpdateObserver.class);

    @Override
    public void update(CrmEvent event) {
        if (!(event instanceof ActivityCompletedEvent)) return;

        ActivityCompletedEvent e = (ActivityCompletedEvent) event;
        Activity activity = e.getActivity();

        if (activity == null || activity.getContactId() == null) return;

        try {
            String activityCode = mapActivityToCode(activity);
            ContactService.getInstance()
                    .updateLeadScoreFromActivity(activity.getContactId(), activityCode);
            logger.debug("Scor lead actualizat pentru contact {} după activitate {}",
                    activity.getContactId(), activityCode);
        } catch (Exception ex) {
            logger.error("Eroare actualizare scor lead", ex);
        }
    }

    private String mapActivityToCode(Activity activity) {
        if (activity.getActivityType() == null) return "OTHER";

        switch (activity.getActivityType()) {
            case EMAIL: return "EMAIL_OPENED";
            case CALL: return "CALLBACK_REQUESTED";
            case MEETING: return "MEETING_ATTENDED";
            case DEMO: return "DEMO_REQUESTED";
            default: return "OTHER";
        }
    }
}
