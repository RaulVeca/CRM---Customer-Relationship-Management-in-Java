package crm.command.opportunity;

import crm.command.AbstractCommand;
import crm.model.enums.OpportunityStage;
import crm.service.opportunity.OpportunityService;

public class MoveOpportunityStageCommand extends AbstractCommand<Void> {

    private final Long opportunityId;
    private final OpportunityStage newStage;

    public MoveOpportunityStageCommand(Long opportunityId, OpportunityStage newStage) {
        this.opportunityId = opportunityId;
        this.newStage = newStage;
    }

    @Override
    protected Void doExecute() {
        OpportunityService.getInstance().moveToStage(opportunityId, newStage);
        return null;
    }
}
