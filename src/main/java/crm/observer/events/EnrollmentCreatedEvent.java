package crm.observer.events;

import crm.model.entity.Enrollment;
import crm.observer.CrmEvent;

public class EnrollmentCreatedEvent extends CrmEvent {

    public static final String EVENT_TYPE = "ENROLLMENT_CREATED";

    public EnrollmentCreatedEvent(Enrollment enrollment, String source) {
        super(source, enrollment);
    }

    @Override
    public String getEventType() {
        return EVENT_TYPE;
    }

    public Enrollment getEnrollment() {
        return getPayloadAs(Enrollment.class);
    }
}
