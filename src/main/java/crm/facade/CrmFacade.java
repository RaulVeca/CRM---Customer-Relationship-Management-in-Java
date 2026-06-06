package crm.facade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import crm.command.CommandInvoker;
import crm.command.contact.CreateContactCommand;
import crm.command.contact.UpdateLeadStatusCommand;
import crm.command.enrollment.EnrollContactCommand;
import crm.command.opportunity.CreateOpportunityCommand;
import crm.command.opportunity.MoveOpportunityStageCommand;
import crm.model.entity.*;
import crm.model.enums.*;
import crm.service.activity.ActivityService;
import crm.service.contact.ContactService;
import crm.service.course.CourseService;
import crm.service.enrollment.EnrollmentService;
import crm.service.opportunity.OpportunityService;
import crm.strategy.PricingStrategy;

import java.util.List;
import java.util.Optional;

/**
 * FACADE PATTERN - CrmFacade
 * 
 * Oferă o interfață unică și simplificată pentru întregul sistem CRM.
 * Ascunde complexitatea sub-sistemelor (services, commands, repositories).
 * 
 * Client-ul interacționează doar cu această fațadă pentru majoritatea
 * operațiilor uzuale. Pentru operații complexe poate accesa direct
 * service-urile specifice.
 * 
 * Avantaje:
 * - Punct unic de intrare în sistem
 * - Decuplare client de sub-sisteme
 * - API simplificat pentru operații comune
 * - Logging și măsurare uniformă
 */
public class CrmFacade {

    private static final Logger logger = LoggerFactory.getLogger(CrmFacade.class);
    private static volatile CrmFacade instance;

    private final ContactService contactService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final OpportunityService opportunityService;
    private final ActivityService activityService;
    private final CommandInvoker commandInvoker;

    private CrmFacade() {
        this.contactService = ContactService.getInstance();
        this.courseService = CourseService.getInstance();
        this.enrollmentService = EnrollmentService.getInstance();
        this.opportunityService = OpportunityService.getInstance();
        this.activityService = ActivityService.getInstance();
        this.commandInvoker = CommandInvoker.getInstance();
        logger.info("CrmFacade inițializat");
    }

    public static CrmFacade getInstance() {
        if (instance == null) {
            synchronized (CrmFacade.class) {
                if (instance == null) {
                    instance = new CrmFacade();
                }
            }
        }
        return instance;
    }

    // =====================================================
    // CONTACTS - Operații simplificate
    // =====================================================

    public Contact createContact(Contact contact) {
        return commandInvoker.invoke(new CreateContactCommand(contact));
    }

    public Optional<Contact> findContactByEmail(String email) {
        return contactService.findByEmail(email);
    }

    public Contact getContact(Long id) {
        return contactService.getById(id);
    }

    public List<Contact> searchContacts(String query, int page, int pageSize) {
        return contactService.searchContacts(query, page, pageSize);
    }

    public List<Contact> getHotLeads(int limit) {
        return contactService.getHotLeads(limit);
    }

    public void changeLeadStatus(Long contactId, LeadStatus newStatus) {
        commandInvoker.invoke(new UpdateLeadStatusCommand(contactId, newStatus));
    }

    public void updateLeadScoreFromActivity(Long contactId, String activityType) {
        contactService.updateLeadScoreFromActivity(contactId, activityType);
    }

    // =====================================================
    // COURSES
    // =====================================================

    public Course createCourse(Course course) {
        return courseService.createCourse(course);
    }

    public List<Course> getActiveCourses() {
        return courseService.getAllActiveCourses();
    }

    public List<Course> getCoursesByCategory(CourseCategory category) {
        return courseService.getByCategory(category);
    }

    public Course getCourse(Long id) {
        return courseService.getById(id);
    }

    // =====================================================
    // ENROLLMENTS
    // =====================================================

    public Enrollment enrollContact(Long contactId, Long sessionId, Long courseId, 
                                     PricingStrategy pricingStrategy) {
        Course course = courseService.getById(courseId);
        return commandInvoker.invoke(new EnrollContactCommand(
                contactId, sessionId, course, pricingStrategy));
    }

    public void recordPayment(Long enrollmentId, java.math.BigDecimal amount) {
        enrollmentService.registerPayment(enrollmentId, amount);
    }

    public void completeCourse(Long enrollmentId, java.math.BigDecimal grade) {
        enrollmentService.completeEnrollment(enrollmentId, grade);
    }

    public List<Enrollment> getEnrollmentsForContact(Long contactId) {
        return enrollmentService.getByContactId(contactId);
    }

    public List<Enrollment> getUnpaidEnrollments() {
        return enrollmentService.getUnpaid();
    }

    // =====================================================
    // OPPORTUNITIES (B2B)
    // =====================================================

    public Opportunity createOpportunity(Opportunity opportunity) {
        return commandInvoker.invoke(new CreateOpportunityCommand(opportunity));
    }

    public void moveOpportunityStage(Long opportunityId, OpportunityStage newStage) {
        commandInvoker.invoke(new MoveOpportunityStageCommand(opportunityId, newStage));
    }

    public void markOpportunityAsLost(Long opportunityId, String reason) {
        opportunityService.markAsLost(opportunityId, reason);
    }

    public List<Opportunity> getActivePipeline() {
        return opportunityService.getActivePipeline();
    }

    public List<Opportunity> getOpportunitiesByStage(OpportunityStage stage) {
        return opportunityService.getByStage(stage);
    }

    public List<Opportunity> getOpportunitiesForClient(Long clientId) {
        return opportunityService.getByClient(clientId);
    }

    // =====================================================
    // ACTIVITIES
    // =====================================================

    public Activity logActivity(Activity activity) {
        return activityService.createActivity(activity);
    }

    public void completeActivity(Long activityId, String outcome, String nextSteps) {
        activityService.completeActivity(activityId, outcome, nextSteps);
    }

    public List<Activity> getActivitiesForContact(Long contactId) {
        return activityService.getByContact(contactId);
    }

    public List<Activity> getUpcomingActivities(Long userId, int days) {
        return activityService.getUpcomingForUser(userId, days);
    }

    // =====================================================
    // STATISTICS
    // =====================================================

    public long getTotalContacts() {
        return contactService.countTotal();
    }

    public long getContactsByStatus(LeadStatus status) {
        return contactService.countByStatus(status);
    }
}
