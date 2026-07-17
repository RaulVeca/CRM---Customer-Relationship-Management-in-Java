package crm.service.review;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import crm.dao.CourseSessionDao;
import crm.exception.BusinessException;
import crm.model.entity.Contact;
import crm.model.entity.Course;
import crm.model.entity.CourseSession;
import crm.model.entity.Enrollment;
import crm.model.enums.ContactType;
import crm.model.enums.DeliveryMode;
import crm.model.enums.LeadSource;
import crm.model.enums.SessionStatus;
import crm.service.contact.ContactService;
import crm.service.course.CourseService;
import crm.service.enrollment.EnrollmentService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * SINGLETON - ReviewService.
 *
 * <p>Orchestrează fluxul public de "cumpărare curs" și de "recenzie curs" pentru
 * site-ul web. O recenzie poate fi lăsată <b>doar după ce clientul a cumpărat
 * cursul</b> (adică are o înscriere - {@link Enrollment} - pe una dintre sesiunile
 * cursului). Recenzia este memorată pe înscriere (câmpurile {@code rating} și
 * {@code feedback}), evitând o tabelă suplimentară.</p>
 *
 * <p>Clientul este identificat prin email (site-ul nu are autentificare de
 * cursant). La cumpărare, contactul este creat dacă nu există încă.</p>
 */
public class ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);
    private static volatile ReviewService instance;

    private static final String NOT_PURCHASED =
            "You can only leave a review after you've purchased this course.";

    private final ContactService contactService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final CourseSessionDao sessionDao;

    private ReviewService() {
        this.contactService = ContactService.getInstance();
        this.courseService = CourseService.getInstance();
        this.enrollmentService = EnrollmentService.getInstance();
        this.sessionDao = CourseSessionDao.getInstance();
    }

    public static ReviewService getInstance() {
        if (instance == null) {
            synchronized (ReviewService.class) {
                if (instance == null) {
                    instance = new ReviewService();
                }
            }
        }
        return instance;
    }

    // =====================================================
    // PURCHASE
    // =====================================================

    /**
     * Înregistrează cumpărarea unui curs de către un client (identificat prin email).
     * Creează contactul dacă nu există, creează/refolosește o sesiune pentru curs,
     * generează înscrierea și o marchează ca plătită. Idempotent: dacă acel client
     * a cumpărat deja cursul, întoarce înscrierea existentă.
     */
    public Enrollment purchaseCourse(String email, String firstName, String lastName, Long courseId) {
        Course course = courseService.getById(courseId);
        Contact contact = findOrCreateContact(email, firstName, lastName);

        Optional<Enrollment> existing = findEnrollmentForCourse(contact.getId(), courseId);
        if (existing.isPresent()) {
            logger.info("Purchase ignored - client {} already enrolled in course {}", email, courseId);
            return existing.get();
        }

        CourseSession session = resolveSession(course);
        Enrollment enrollment = enrollmentService.enrollContact(contact.getId(), session.getId());

        logger.info("Course purchased: client={}, course={}, enrollment={}", email, courseId, enrollment.getId());
        return enrollment;
    }

    // =====================================================
    // REVIEW
    // =====================================================

    /**
     * Salvează o recenzie (1-5 stele + comentariu) pentru un curs. Permis doar dacă
     * acel client (după email) a cumpărat cursul.
     */
    public Enrollment reviewCourse(String email, Long courseId, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("The rating must be between 1 and 5 stars");
        }
        // confirmă existența cursului (404 dacă lipsește)
        courseService.getById(courseId);

        Contact contact = contactService.findByEmail(email)
                .orElseThrow(() -> new BusinessException(NOT_PURCHASED));
        Enrollment enrollment = findEnrollmentForCourse(contact.getId(), courseId)
                .orElseThrow(() -> new BusinessException(NOT_PURCHASED));

        return enrollmentService.saveReview(enrollment.getId(), rating, comment);
    }

    /**
     * Cursurile cumpărate de un client (identificat prin email), cele mai noi
     * primele. Fiecare curs apare o singură dată (cumpărarea e idempotentă),
     * însoțit de suma plătită, data cumpărării și recenzia proprie (dacă există).
     * Stă la baza paginii personale "cursurile mele" și a marcării butoanelor
     * "already bought" din catalog.
     */
    public List<PurchasedCourse> getPurchasedCourses(String email) {
        Optional<Contact> contact = contactService.findByEmail(email);
        if (contact.isEmpty()) {
            return List.of();
        }
        List<Enrollment> enrollments = new ArrayList<>(
                enrollmentService.getByContactId(contact.get().getId()));
        enrollments.sort(Comparator.comparing(
                Enrollment::getEnrollmentDate,
                Comparator.nullsLast(Comparator.reverseOrder())));

        Map<Long, PurchasedCourse> byCourse = new LinkedHashMap<>();
        for (Enrollment e : enrollments) {
            CourseSession session = sessionDao.findById(e.getSessionId()).orElse(null);
            if (session == null) continue;
            Course course;
            try {
                course = courseService.getById(session.getCourseId());
            } catch (RuntimeException ex) {
                continue;
            }
            // most recent enrollment wins (list is already newest-first)
            byCourse.putIfAbsent(course.getId(), new PurchasedCourse(
                    course, e.getEnrollmentDate(), e.getRating()));
        }
        return new ArrayList<>(byCourse.values());
    }

    /** Toate recenziile publicate pentru un curs, cele mai noi primele. */
    public List<CourseReviewView> getReviews(Long courseId) {
        List<CourseReviewView> reviews = new ArrayList<>();
        for (Long sessionId : sessionIdsForCourse(courseId)) {
            for (Enrollment e : enrollmentService.getBySessionId(sessionId)) {
                if (e.getRating() == null) continue;
                String author = contactService.findById(e.getContactId())
                        .flatMap(Contact::getFullName)
                        .orElse("Cursant anonim");
                reviews.add(new CourseReviewView(
                        author, e.getRating(), e.getFeedback(), e.getEnrollmentDate()));
            }
        }
        reviews.sort(Comparator.comparing(
                CourseReviewView::date,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return reviews;
    }

    /**
     * Rezumatul ratingului (media + numărul de recenzii) pentru un curs. Un
     * {@code rating} de 0 înseamnă "cumpărat, dar nerecenzat încă", deci nu
     * intră nici în medie, nici în numărătoare.
     */
    public RatingSummary getRatingSummary(Long courseId) {
        int count = 0;
        int sum = 0;
        for (Long sessionId : sessionIdsForCourse(courseId)) {
            for (Enrollment e : enrollmentService.getBySessionId(sessionId)) {
                if (e.getRating() == null || e.getRating() < 1) continue;
                count++;
                sum += e.getRating();
            }
        }
        double average = count == 0 ? 0.0 : Math.round((double) sum / count * 10.0) / 10.0;
        return new RatingSummary(average, count);
    }

    /**
     * Cifrele reale afișate în antetul site-ului: câte cursuri active există,
     * câți cursanți distincți s-au înscris pe ele și media tuturor recenziilor.
     * Ca și la {@link #getRatingSummary}, un rating de 0 înseamnă "nerecenzat"
     * și este ignorat.
     */
    public SiteStats getSiteStats() {
        List<Course> courses = courseService.getAllActiveCourses();

        // Doar înscrierile pe sesiunile cursurilor active contează, ca numărul
        // de cursanți să corespundă catalogului pe care îl vede vizitatorul.
        Set<Long> activeSessionIds = courses.stream()
                .flatMap(c -> sessionIdsForCourse(c.getId()).stream())
                .collect(Collectors.toSet());

        Set<Long> learners = new HashSet<>();
        int count = 0;
        int sum = 0;
        for (Enrollment e : enrollmentService.getAll()) {
            if (!activeSessionIds.contains(e.getSessionId())) continue;
            learners.add(e.getContactId());
            if (e.getRating() == null || e.getRating() < 1) continue;
            count++;
            sum += e.getRating();
        }

        double average = count == 0 ? 0.0 : Math.round((double) sum / count * 10.0) / 10.0;
        return new SiteStats(courses.size(), learners.size(), average, count);
    }

    // =====================================================
    // HELPERS
    // =====================================================

    /**
     * Resolves the contact for a client email, creating a minimal INDIVIDUAL
     * contact the first time. Shared by the public purchase flow and employee
     * sign-in, so an employee gets exactly the same client-portal contact
     * whether they buy a course or simply log in.
     */
    public Contact findOrCreateContact(String email, String firstName, String lastName) {
        return contactService.findByEmail(email).orElseGet(() -> {
            String first = blankToNull(firstName);
            String last = blankToNull(lastName);
            // An INDIVIDUAL contact requires a name. Buyers who aren't a contact
            // yet (e.g. an employee) may purchase without one being supplied, so
            // fall back to the email's local part instead of failing validation.
            if (first == null && last == null) {
                first = nameFromEmail(email);
            }
            Contact contact = Contact.builder()
                    .contactType(ContactType.INDIVIDUAL)
                    .firstName(first)
                    .lastName(last)
                    .email(email)
                    .leadSource(LeadSource.WEBSITE)
                    .gdprConsent(Boolean.TRUE)
                    .gdprConsentDate(LocalDateTime.now())
                    .build();
            return contactService.createContact(contact);
        });
    }

    /** Derives a non-empty display name from an email's local part. */
    private static String nameFromEmail(String email) {
        if (email == null) return "Client";
        int at = email.indexOf('@');
        String local = (at > 0 ? email.substring(0, at) : email).trim();
        return local.isEmpty() ? "Client" : local;
    }

    /** Întoarce o sesiune existentă a cursului sau creează una self-paced (online). */
    private CourseSession resolveSession(Course course) {
        List<CourseSession> sessions = sessionDao.findByCourseId(course.getId());
        if (!sessions.isEmpty()) {
            return sessions.get(0);
        }
        String baseCode = course.getCode() != null ? course.getCode() : "COURSE-" + course.getId();
        CourseSession session = CourseSession.builder()
                .courseId(course.getId())
                .sessionCode(baseCode + "-WEB")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(3))
                .scheduleDescription("Self-paced (online)")
                .totalHours(course.getDurationHours() != null ? course.getDurationHours() : 0)
                .deliveryMode(DeliveryMode.ONLINE)
                .maxParticipants(course.getMaxParticipants() != null ? course.getMaxParticipants() : 1000)
                .currentParticipants(0)
                .status(SessionStatus.OPEN_ENROLLMENT)
                .isCorporate(Boolean.FALSE)
                .build();
        return sessionDao.save(session);
    }

    private Optional<Enrollment> findEnrollmentForCourse(Long contactId, Long courseId) {
        Set<Long> sessionIds = sessionIdsForCourse(courseId);
        return enrollmentService.getByContactId(contactId).stream()
                .filter(e -> sessionIds.contains(e.getSessionId()))
                .max(Comparator.comparing(
                        Enrollment::getEnrollmentDate,
                        Comparator.nullsFirst(Comparator.naturalOrder())));
    }

    private Set<Long> sessionIdsForCourse(Long courseId) {
        return sessionDao.findByCourseId(courseId).stream()
                .map(CourseSession::getId)
                .collect(Collectors.toSet());
    }

    private static String blankToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    /** Proiecție domeniu a unei recenzii (independentă de stratul web). */
    public record CourseReviewView(String author, int rating, String comment, LocalDateTime date) {}

    /** Media ratingului și numărul de recenzii pentru un curs. */
    public record RatingSummary(double average, int count) {}

    /** Un curs cumpărat de un client, cu detaliile cumpărării. */
    public record PurchasedCourse(Course course, LocalDateTime date, Integer rating) {}

    /** Cifrele reale ale site-ului, pentru secțiunea de statistici din antet. */
    public record SiteStats(int courseCount, int learnerCount, double averageRating, int reviewCount) {}
}
