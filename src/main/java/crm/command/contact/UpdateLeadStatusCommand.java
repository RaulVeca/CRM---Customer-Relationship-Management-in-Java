package crm.command.contact;

import crm.command.AbstractCommand;
import crm.model.enums.LeadStatus;
import crm.service.contact.ContactService;

public class UpdateLeadStatusCommand extends AbstractCommand<Void> {

    private final Long contactId;
    private final LeadStatus newStatus;

    public UpdateLeadStatusCommand(Long contactId, LeadStatus newStatus) {
        this.contactId = contactId;
        this.newStatus = newStatus;
    }

    @Override
    protected Void doExecute() {
        ContactService.getInstance().updateLeadStatus(contactId, newStatus);
        return null;
    }
}
