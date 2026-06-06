package crm.observer.events;

import crm.model.entity.Activity;
import crm.observer.CrmEvent;

public class ActivityCompletedEvent extends CrmEvent {

    public static final String EVENT_TYPE = "ACTIVITY_COMPLETED";

    public ActivityCompletedEvent(Activity activity, String source) {
        super(source, activity);
    }

    @Override
    public String getEventType() {
        return EVENT_TYPE;
    }

    public Activity getActivity() {
        return getPayloadAs(Activity.class);
    }
}
