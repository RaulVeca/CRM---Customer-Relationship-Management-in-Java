package crm.command.enrollment;

import crm.command.AbstractCommand;
import crm.model.entity.Course;
import crm.model.entity.Enrollment;
import crm.service.enrollment.EnrollmentService;
import crm.strategy.PricingStrategy;

public class EnrollContactCommand extends AbstractCommand<Enrollment> {

    private final Long contactId;
    private final Long sessionId;
    private final Course course;
    private final PricingStrategy pricingStrategy;

    public EnrollContactCommand(Long contactId, Long sessionId, Course course, 
                                 PricingStrategy pricingStrategy) {
        this.contactId = contactId;
        this.sessionId = sessionId;
        this.course = course;
        this.pricingStrategy = pricingStrategy;
    }

    @Override
    protected Enrollment doExecute() {
        return EnrollmentService.getInstance()
                .enrollContact(contactId, sessionId, course, pricingStrategy);
    }
}
