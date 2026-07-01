package crm.service.invoice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import crm.dao.EmployeeDao;
import crm.model.entity.Invoice;
import crm.model.entity.MeditationSession;
import crm.model.enums.PaymentStatus;
import crm.repository.InvoiceRepository;
import crm.web.dto.AuthResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * SINGLETON - InvoiceService.
 *
 * <p>Generează automat câte o factură la fiecare rezervare de ședință online cu un
 * trainer (booking). Este apelat de
 * {@link crm.observer.listeners.InvoiceGenerationObserver} pe evenimentul
 * {@code SESSION_BOOKED}.</p>
 *
 * <p>Suma variază: {@code subtotal = ore × tarif/oră}. Conturile de angajat primesc
 * discountul automat de {@value crm.web.dto.AuthResponse#EMPLOYEE_DISCOUNT_RATE}
 * (−60%), determinat aici după cum emailul rezervării apare sau nu în tabelul
 * {@code employees} — exact regula folosită la autentificare. Fără TVA, în USD:
 * totalul facturii este fix suma pe care clientul o transferă la programare.</p>
 */
public class InvoiceService {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceService.class);
    private static volatile InvoiceService instance;

    /** Tariful pe oră (USD), identic cu {@code PRICE_PER_HOUR} din pagina de programare. */
    public static final BigDecimal HOURLY_RATE = new BigDecimal("10");

    private final InvoiceRepository invoiceRepository;
    private final EmployeeDao employeeDao;

    private InvoiceService() {
        this.invoiceRepository = InvoiceRepository.getInstance();
        this.employeeDao = EmployeeDao.getInstance();
    }

    public static InvoiceService getInstance() {
        if (instance == null) {
            synchronized (InvoiceService.class) {
                if (instance == null) {
                    instance = new InvoiceService();
                }
            }
        }
        return instance;
    }

    /**
     * Generează și persistă o factură pentru rezervarea de ședință dată. Idempotent:
     * dacă ședința are deja o factură, o returnează pe aceea în loc să creeze un
     * duplicat.
     */
    public Invoice generateForSession(MeditationSession session) {
        if (session == null || session.getId() == null) {
            throw new IllegalArgumentException("Nu se poate genera factură fără o ședință salvată");
        }

        List<Invoice> existing = invoiceRepository.findBySessionId(session.getId());
        if (!existing.isEmpty()) {
            logger.info("Factură deja existentă pentru ședința {} - se refolosește", session.getId());
            return existing.get(0);
        }

        int hours = session.getDurationHours();
        BigDecimal subtotal = HOURLY_RATE.multiply(BigDecimal.valueOf(hours)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discountRate = discountRateFor(session.getContactEmail());
        BigDecimal discountAmount = subtotal.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.subtract(discountAmount);
        LocalDate issueDate = LocalDate.now();

        // Rezervarea se face doar după ce banii au fost transferați, deci factura
        // este de la început plătită integral (o chitanță).
        Invoice invoice = Invoice.builder()
                .invoiceNumber(buildInvoiceNumber(issueDate, session.getId()))
                .sessionId(session.getId())
                .clientId(session.getContactId())
                .clientEmail(session.getContactEmail())
                .issueDate(issueDate)
                .hours(hours)
                .hourlyRate(HOURLY_RATE.setScale(2, RoundingMode.HALF_UP))
                .subtotal(subtotal)
                .discountRate(discountRate)
                .discountAmount(discountAmount)
                .total(total)
                .paidAmount(total)
                .status(PaymentStatus.PAID)
                .paymentDate(issueDate)
                .description(buildDescription(session, discountRate))
                .build();

        Invoice saved = invoiceRepository.save(invoice);
        logger.info("Factură generată automat: {} pentru ședința {} ({}h × ${} − {}% = ${})",
                saved.getInvoiceNumber(), session.getId(), hours, HOURLY_RATE,
                discountRate.multiply(BigDecimal.valueOf(100)).intValue(), total);
        return saved;
    }

    public Invoice getById(Long id) {
        return invoiceRepository.getById(id);
    }

    public List<Invoice> getByClientId(Long clientId) {
        return invoiceRepository.findByClientId(clientId);
    }

    public List<Invoice> getBySessionId(Long sessionId) {
        return invoiceRepository.findBySessionId(sessionId);
    }

    public List<Invoice> getAll() {
        return invoiceRepository.findAll();
    }

    /** −60% pentru conturile de angajat (email prezent în {@code employees}), altfel 0. */
    private BigDecimal discountRateFor(String email) {
        if (email != null && employeeDao.findByEmail(email.trim().toLowerCase()).isPresent()) {
            return BigDecimal.valueOf(AuthResponse.EMPLOYEE_DISCOUNT_RATE);
        }
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private String buildDescription(MeditationSession s, BigDecimal discountRate) {
        // ASCII-safe (fără diacritice) ca textul să nu depindă de charset-ul conexiunii DB.
        String base = "Sedinta online cu trainer (" + s.getSessionDate() + ", "
                + s.getStartHour() + ":00-" + s.getEndHour() + ":00, " + s.getDurationHours() + "h)";
        if (discountRate.signum() > 0) {
            base += " - discount angajat " + discountRate.multiply(BigDecimal.valueOf(100)).intValue() + "%";
        }
        return base;
    }

    /** Ex.: {@code INV-2026-00042} - unic, întrucât id-ul ședinței este unic. */
    private String buildInvoiceNumber(LocalDate issueDate, Long sessionId) {
        return String.format("INV-%d-%05d", issueDate.getYear(), sessionId);
    }
}
