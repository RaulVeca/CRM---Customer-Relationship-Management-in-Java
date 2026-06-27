package crm.service.enrollment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import crm.exception.BusinessException;
import crm.model.entity.Contact;
import crm.model.entity.Course;
import crm.model.entity.Enrollment;
import crm.model.enums.EnrollmentStatus;
import crm.model.enums.LeadStatus;
import crm.model.enums.PaymentStatus;
import crm.observer.EventBus;
import crm.observer.events.EnrollmentCreatedEvent;
import crm.repository.ContactRepository;
import crm.repository.EnrollmentRepository;
import crm.service.contact.ContactService;
import crm.strategy.IndividualPricingStrategy;
import crm.strategy.PricingStrategy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * SINGLETON - EnrollmentService.
 * Folosește Strategy Pattern pentru calcul preț și Observer pentru notificări.
 */
public class EnrollmentService {

    private static final Logger logger = LoggerFactory.getLogger(EnrollmentService.class);
    private static volatile EnrollmentService instance;

    private final EnrollmentRepository enrollmentRepository;
    private final ContactRepository contactRepository;
    private final EventBus eventBus;

    private EnrollmentService() {
        this.enrollmentRepository = EnrollmentRepository.getInstance();
        this.contactRepository = ContactRepository.getInstance();
        this.eventBus = EventBus.getInstance();
    }

    public static EnrollmentService getInstance() {
        if (instance == null) {
            synchronized (EnrollmentService.class) {
                if (instance == null) {
                    instance = new EnrollmentService();
                }
            }
        }
        return instance;
    }

    /**
     * Creează o înscriere folosind strategia de pricing furnizată.
     */
    public Enrollment enrollContact(Long contactId, Long sessionId, Course course, 
                                     PricingStrategy pricingStrategy) {
        Contact contact = contactRepository.getById(contactId);

        // Verifică dacă deja există înscriere
        boolean alreadyEnrolled = enrollmentRepository.findByContactId(contactId).stream()
                .anyMatch(e -> e.getSessionId().equals(sessionId));
        if (alreadyEnrolled) {
            throw new BusinessException("Contactul este deja înscris la această sesiune");
        }

        // STRATEGY PATTERN - calcul preț
        if (pricingStrategy == null) {
            pricingStrategy = new IndividualPricingStrategy(false, false);
        }
        BigDecimal price = course.getPriceIndividual();
        BigDecimal discount = pricingStrategy.calculateDiscount(price, 1);
        BigDecimal finalPrice = pricingStrategy.calculatePrice(course, 1);

        Enrollment enrollment = Enrollment.builder()
                .sessionId(sessionId)
                .contactId(contactId)
                .enrollmentDate(LocalDateTime.now())
                .status(EnrollmentStatus.PENDING)
                .price(price)
                .discount(discount)
                .finalPrice(finalPrice)
                .paymentStatus(PaymentStatus.UNPAID)
                .paidAmount(BigDecimal.ZERO)
                .build();

        Enrollment saved = enrollmentRepository.save(enrollment);

        // Schimbă statusul lead-ului la ENROLLED
        ContactService.getInstance().updateLeadStatus(contactId, LeadStatus.ENROLLED);

        // OBSERVER - notifică crearea înscrierii
        eventBus.publish(new EnrollmentCreatedEvent(saved, "EnrollmentService"));

        logger.info("Înscriere creată: contact={}, sesiune={}, preț={}", 
                contactId, sessionId, finalPrice);
        return saved;
    }

    /**
     * Înregistrează o plată parțială sau totală.
     */
    public void registerPayment(Long enrollmentId, BigDecimal amount) {
        Enrollment enrollment = enrollmentRepository.getById(enrollmentId);

        BigDecimal currentPaid = enrollment.getPaidAmount() != null 
                ? enrollment.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal newPaid = currentPaid.add(amount);

        enrollment.setPaidAmount(newPaid);

        if (newPaid.compareTo(enrollment.getFinalPrice()) >= 0) {
            enrollment.setPaymentStatus(PaymentStatus.PAID);
            enrollment.setStatus(EnrollmentStatus.CONFIRMED);
        } else if (newPaid.compareTo(BigDecimal.ZERO) > 0) {
            enrollment.setPaymentStatus(PaymentStatus.PARTIAL);
        }

        enrollmentRepository.save(enrollment);
        logger.info("Plată înregistrată: enrollment={}, amount={}, total={}", 
                enrollmentId, amount, newPaid);
    }

    /**
     * Marchează cursul ca fiind finalizat și emite certificat.
     */
    public void completeEnrollment(Long enrollmentId, BigDecimal grade) {
        Enrollment enrollment = enrollmentRepository.getById(enrollmentId);

        enrollment.setStatus(EnrollmentStatus.COMPLETED);
        enrollment.setFinalGrade(grade);
        enrollment.setExamPassed(grade.compareTo(new BigDecimal("5")) >= 0);

        if (enrollment.getExamPassed()) {
            enrollment.setCertificateIssued(true);
            enrollment.setCertificateNumber("CERT-" + System.currentTimeMillis() + "-" + enrollmentId);
        }

        enrollmentRepository.save(enrollment);
        logger.info("Înscriere finalizată: id={}, grade={}, passed={}", 
                enrollmentId, grade, enrollment.getExamPassed());
    }

    /**
     * Salvează review-ul (rating 1-5 + feedback) lăsat de cursant pe înscrierea sa.
     * Folosit de fluxul public de recenzii al site-ului.
     */
    public Enrollment saveReview(Long enrollmentId, int rating, String feedback) {
        if (rating < 1 || rating > 5) {
            throw new BusinessException("Rating-ul trebuie să fie între 1 și 5 stele");
        }
        Enrollment enrollment = enrollmentRepository.getById(enrollmentId);
        enrollment.setRating(rating);
        enrollment.setFeedback(feedback);
        Enrollment saved = enrollmentRepository.save(enrollment);
        logger.info("Review salvat: enrollment={}, rating={}", enrollmentId, rating);
        return saved;
    }

    public List<Enrollment> getByContactId(Long contactId) {
        return enrollmentRepository.findByContactId(contactId);
    }

    public List<Enrollment> getBySessionId(Long sessionId) {
        return enrollmentRepository.findBySessionId(sessionId);
    }

    public List<Enrollment> getUnpaid() {
        return enrollmentRepository.findUnpaid();
    }
}
