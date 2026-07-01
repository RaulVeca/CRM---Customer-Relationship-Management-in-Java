package crm.observer.events;

import crm.model.entity.MeditationSession;
import crm.observer.CrmEvent;

/**
 * Emis când un contact rezervă o ședință online cu un trainer (booking).
 * Declanșează generarea automată a facturii.
 */
public class SessionBookedEvent extends CrmEvent {

    public static final String EVENT_TYPE = "SESSION_BOOKED";

    public SessionBookedEvent(MeditationSession session, String source) {
        super(source, session);
    }

    @Override
    public String getEventType() {
        return EVENT_TYPE;
    }

    public MeditationSession getSession() {
        return getPayloadAs(MeditationSession.class);
    }
}
