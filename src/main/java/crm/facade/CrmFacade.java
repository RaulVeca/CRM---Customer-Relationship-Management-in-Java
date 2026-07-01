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
import crm.service.employee.EmployeeService;
import crm.service.enrollment.EnrollmentService;
import crm.service.invoice.InvoiceService;
import crm.service.analytics.AnalyticsService;
import crm.service.analytics.MetricsService;
import crm.service.enrollment.PurchaseHistoryService;
import crm.service.opportunity.OpportunityService;
import crm.service.review.ReviewService;

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
    private final InvoiceService invoiceService;
    private final OpportunityService opportunityService;
    private final ActivityService activityService;
    private final EmployeeService employeeService;
    private final ReviewService reviewService;
    private final PurchaseHistoryService purchaseHistoryService;
    private final AnalyticsService analyticsService;
    private final MetricsService metricsService;
    private final CommandInvoker commandInvoker;

    private CrmFacade() {
        this.contactService = ContactService.getInstance();
        this.courseService = CourseService.getInstance();
        this.enrollmentService = EnrollmentService.getInstance();
        this.invoiceService = InvoiceService.getInstance();
        this.opportunityService = OpportunityService.getInstance();
        this.activityService = ActivityService.getInstance();
        this.employeeService = EmployeeService.getInstance();
        this.reviewService = ReviewService.getInstance();
        this.purchaseHistoryService = PurchaseHistoryService.getInstance();
        this.analyticsService = AnalyticsService.getInstance();
        this.metricsService = MetricsService.getInstance();
        this.commandInvoker = CommandInvoker.getInstance();
        logger.info("CrmFacade initialized");
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

    public List<Contact> getAllContacts(int page, int pageSize) {
        return contactService.getContactsPage(page, pageSize);
    }

    public List<Contact> listContactsByStatus(LeadStatus status) {
        return contactService.getContactsByStatus(status);
    }

    public Contact updateContact(Contact contact) {
        return contactService.updateContact(contact);
    }

    public boolean deleteContact(Long id) {
        return contactService.deleteContact(id);
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

    public Course updateCourse(Course course) {
        return courseService.updateCourse(course);
    }

    public void deactivateCourse(Long id) {
        courseService.deactivateCourse(id);
    }

    public Optional<Course> findCourseByCode(String code) {
        return courseService.findByCode(code);
    }

    // =====================================================
    // ENROLLMENTS
    // =====================================================

    public Enrollment enrollContact(Long contactId, Long sessionId) {
        return commandInvoker.invoke(new EnrollContactCommand(contactId, sessionId));
    }

    public void completeCourse(Long enrollmentId, java.math.BigDecimal grade) {
        enrollmentService.completeEnrollment(enrollmentId, grade);
    }

    public List<Enrollment> getEnrollmentsForContact(Long contactId) {
        return enrollmentService.getByContactId(contactId);
    }

    // =====================================================
    // INVOICES - generate automat la fiecare rezervare de ședință (booking)
    // =====================================================

    /** O factură după id. */
    public Invoice getInvoice(Long id) {
        return invoiceService.getById(id);
    }

    /** Toate facturile emise unui client. */
    public List<Invoice> getInvoicesForClient(Long clientId) {
        return invoiceService.getByClientId(clientId);
    }

    /** Factura/facturile generate pentru o rezervare de ședință. */
    public List<Invoice> getInvoicesForSession(Long sessionId) {
        return invoiceService.getBySessionId(sessionId);
    }

    /** Toate facturile (vedere admin). */
    public List<Invoice> getAllInvoices() {
        return invoiceService.getAll();
    }

    // =====================================================
    // COURSE REVIEWS (public website)
    // =====================================================

    /** Cumpărarea publică a unui curs (client identificat prin email). */
    public Enrollment purchaseCourse(String email, String firstName, String lastName, Long courseId) {
        return reviewService.purchaseCourse(email, firstName, lastName, courseId);
    }

    /** Recenzie publică (1-5 stele) - permisă doar după cumpărare. */
    public Enrollment reviewCourse(String email, Long courseId, int rating, String comment) {
        return reviewService.reviewCourse(email, courseId, rating, comment);
    }

    public List<ReviewService.CourseReviewView> getCourseReviews(Long courseId) {
        return reviewService.getReviews(courseId);
    }

    public ReviewService.RatingSummary getCourseRating(Long courseId) {
        return reviewService.getRatingSummary(courseId);
    }

    /** Cursurile cumpărate de un client (după email) - pagina personală "cursurile mele". */
    public List<ReviewService.PurchasedCourse> getPurchasedCourses(String email) {
        return reviewService.getPurchasedCourses(email);
    }

    /** Full purchase (enrollment) history for the admin view. */
    public List<PurchaseHistoryService.PurchaseRecord> getPurchaseHistory() {
        return purchaseHistoryService.getHistory();
    }

    // =====================================================
    // ANALYTICS
    // =====================================================

    /** Aggregated admin analytics: demographics, churn and click-through rate. */
    public AnalyticsService.Analytics getAnalytics() {
        return analyticsService.getAnalytics();
    }

    /** Records catalog impressions for the click-through-rate metric. */
    public void recordCourseImpressions(List<Long> courseIds) {
        metricsService.recordImpressions(courseIds);
    }

    /** Records a catalog click-through for the click-through-rate metric. */
    public void recordCourseClick(Long courseId) {
        metricsService.recordClick(courseId);
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

    public Opportunity getOpportunity(Long id) {
        return opportunityService.getById(id);
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
    // EMPLOYEES (of corporate clients)
    // =====================================================

    public Employee addEmployee(Employee employee) {
        return employeeService.addEmployee(employee);
    }

    public Employee updateEmployee(Employee employee) {
        return employeeService.updateEmployee(employee);
    }

    public Employee getEmployee(Long id) {
        return employeeService.getById(id);
    }

    public List<Employee> getEmployeesForCompany(Long companyId) {
        return employeeService.getByCompany(companyId);
    }

    public long countEmployeesForCompany(Long companyId) {
        return employeeService.countByCompany(companyId);
    }

    public boolean deleteEmployee(Long id) {
        return employeeService.deleteEmployee(id);
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
