package crm.command.enrollment;

import crm.command.AbstractCommand;
import crm.model.entity.Enrollment;
import crm.service.enrollment.EnrollmentService;

public class EnrollContactCommand extends AbstractCommand<Enrollment> {

    private final Long contactId;
    private final Long sessionId;

    public EnrollContactCommand(Long contactId, Long sessionId) {
        this.contactId = contactId;
        this.sessionId = sessionId;
    }

    @Override
    protected Enrollment doExecute() {
        return EnrollmentService.getInstance().enrollContact(contactId, sessionId);
    }
}
