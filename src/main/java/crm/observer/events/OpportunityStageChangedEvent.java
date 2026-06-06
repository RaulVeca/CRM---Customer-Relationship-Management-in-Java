package crm.observer.events;

import lombok.Getter;
import crm.model.entity.Opportunity;
import crm.model.enums.OpportunityStage;
import crm.observer.CrmEvent;

@Getter
public class OpportunityStageChangedEvent extends CrmEvent {

    public static final String EVENT_TYPE = "OPPORTUNITY_STAGE_CHANGED";

    private final OpportunityStage oldStage;
    private final OpportunityStage newStage;

    public OpportunityStageChangedEvent(Opportunity opp, OpportunityStage oldStage, 
                                         OpportunityStage newStage, String source) {
        super(source, opp);
        this.oldStage = oldStage;
        this.newStage = newStage;
    }

    @Override
    public String getEventType() {
        return EVENT_TYPE;
    }

    public Opportunity getOpportunity() {
        return getPayloadAs(Opportunity.class);
    }
}
