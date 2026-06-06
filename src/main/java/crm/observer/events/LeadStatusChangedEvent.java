package crm.observer.events;

import crm.model.entity.Contact;
import crm.model.enums.LeadStatus;
import crm.observer.CrmEvent;
import lombok.Getter;

@Getter
public class LeadStatusChangedEvent extends CrmEvent {

    public static final String EVENT_TYPE = "LEAD_STATUS_CHANGED";

    private final LeadStatus oldStatus;
    private final LeadStatus newStatus;

    public LeadStatusChangedEvent(Contact contact, LeadStatus oldStatus, LeadStatus newStatus, String source) {
        super(source, contact);
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    @Override
    public String getEventType() {
        return EVENT_TYPE;
    }

    public Contact getContact() {
        return getPayloadAs(Contact.class);
    }
}
