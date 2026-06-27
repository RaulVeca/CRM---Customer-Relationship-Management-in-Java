package crm.service.enrollment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import crm.dao.CourseSessionDao;
import crm.model.entity.Contact;
import crm.model.entity.Course;
import crm.model.entity.CourseSession;
import crm.model.entity.Enrollment;
import crm.service.contact.ContactService;
import crm.service.course.CourseService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SINGLETON - PurchaseHistoryService.
 *
 * <p>Builds the admin "purchase history" view: every enrollment is an order, so
 * this joins each {@link Enrollment} with its course (via the session) and the
 * buyer ({@link Contact}) to produce a flat, display-ready list of purchases.</p>
 */
public class PurchaseHistoryService {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseHistoryService.class);
    private static volatile PurchaseHistoryService instance;

    private final EnrollmentService enrollmentService;
    private final ContactService contactService;
    private final CourseService courseService;
    private final CourseSessionDao sessionDao;

    private PurchaseHistoryService() {
        this.enrollmentService = EnrollmentService.getInstance();
        this.contactService = ContactService.getInstance();
        this.courseService = CourseService.getInstance();
        this.sessionDao = CourseSessionDao.getInstance();
    }

    public static PurchaseHistoryService getInstance() {
        if (instance == null) {
            synchronized (PurchaseHistoryService.class) {
                if (instance == null) {
                    instance = new PurchaseHistoryService();
                }
            }
        }
        return instance;
    }

    /** All purchases (enrollments), most recent first, enriched for display. */
    public List<PurchaseRecord> getHistory() {
        Map<Long, CourseSession> sessions = new HashMap<>();
        Map<Long, Course> courses = new HashMap<>();
        Map<Long, Contact> contacts = new HashMap<>();

        List<PurchaseRecord> history = new ArrayList<>();
        for (Enrollment e : enrollmentService.getAll()) {
            CourseSession session = sessions.computeIfAbsent(
                    e.getSessionId(),
                    id -> sessionDao.findById(id).orElse(null));
            Course course = session == null ? null : courses.computeIfAbsent(
                    session.getCourseId(), this::lookupCourse);
            Contact contact = contacts.computeIfAbsent(
                    e.getContactId(),
                    id -> contactService.findById(id).orElse(null));

            BigDecimal amount = e.getFinalPrice() != null ? e.getFinalPrice() : e.getPrice();

            history.add(new PurchaseRecord(
                    e.getId(),
                    contact == null ? "(unknown)" : contact.getFullName().orElse("(unknown)"),
                    contact == null ? null : contact.getEmail(),
                    course == null ? "(course removed)" : course.getName(),
                    course == null ? null : course.getCode(),
                    amount,
                    e.getPaymentStatus() == null ? null : e.getPaymentStatus().name(),
                    e.getStatus() == null ? null : e.getStatus().name(),
                    e.getEnrollmentDate(),
                    e.getRating()));
        }

        history.sort(Comparator.comparing(
                PurchaseRecord::date,
                Comparator.nullsLast(Comparator.reverseOrder())));
        logger.debug("Purchase history built: {} orders", history.size());
        return history;
    }

    private Course lookupCourse(Long courseId) {
        try {
            return courseService.getById(courseId);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    /** Display-ready projection of a single purchase. */
    public record PurchaseRecord(
            Long enrollmentId,
            String studentName,
            String studentEmail,
            String courseName,
            String courseCode,
            BigDecimal amount,
            String paymentStatus,
            String status,
            LocalDateTime date,
            Integer rating) {}
}
