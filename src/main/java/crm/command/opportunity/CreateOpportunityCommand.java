package crm.command.opportunity;

import crm.command.AbstractCommand;
import crm.model.entity.Opportunity;
import crm.service.opportunity.OpportunityService;

public class CreateOpportunityCommand extends AbstractCommand<Opportunity> {

    private final Opportunity opportunity;

    public CreateOpportunityCommand(Opportunity opportunity) {
        this.opportunity = opportunity;
    }

    @Override
    protected Opportunity doExecute() {
        return OpportunityService.getInstance().createOpportunity(opportunity);
    }
}
