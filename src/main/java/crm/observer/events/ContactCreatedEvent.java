package crm.observer.events;

import crm.model.entity.Contact;
import crm.observer.CrmEvent;

public class ContactCreatedEvent extends CrmEvent {

    public static final String EVENT_TYPE = "CONTACT_CREATED";

    public ContactCreatedEvent(Contact contact, String source) {
        super(source, contact);
    }

    @Override
    public String getEventType() {
        return EVENT_TYPE;
    }

    public Contact getContact() {
        return getPayloadAs(Contact.class);
    }
}
