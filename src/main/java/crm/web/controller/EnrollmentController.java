package crm.web.controller;

import crm.facade.CrmFacade;
import crm.model.entity.Enrollment;
import crm.strategy.CorporatePricingStrategy;
import crm.strategy.IndividualPricingStrategy;
import crm.strategy.PricingStrategy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST endpoints for course enrollments and their payments.
 *
 * <p>The {@code pricing} field on the enrollment request selects the
 * {@link PricingStrategy} (STRATEGY pattern) applied to compute the final
 * price.</p>
 */
@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final CrmFacade facade;

    public EnrollmentController(CrmFacade facade) {
        this.facade = facade;
    }

    @GetMapping("/by-contact/{contactId}")
    public List<Enrollment> byContact(@PathVariable Long contactId) {
        return facade.getEnrollmentsForContact(contactId);
    }

    @GetMapping("/unpaid")
    public List<Enrollment> unpaid() {
        return facade.getUnpaidEnrollments();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Enrollment enroll(@RequestBody EnrollRequest req) {
        return facade.enrollContact(req.contactId(), req.sessionId(), req.courseId(), req.toStrategy());
    }

    @PatchMapping("/{id}/payment")
    public ResponseEntity<Void> recordPayment(@PathVariable Long id, @RequestParam BigDecimal amount) {
        facade.recordPayment(id, amount);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<Void> complete(@PathVariable Long id, @RequestParam BigDecimal grade) {
        facade.completeCourse(id, grade);
        return ResponseEntity.noContent().build();
    }

    /**
     * @param pricing one of INDIVIDUAL or CORPORATE (defaults to INDIVIDUAL)
     */
    public record EnrollRequest(Long contactId, Long sessionId, Long courseId, String pricing) {
        public PricingStrategy toStrategy() {
            if ("CORPORATE".equalsIgnoreCase(pricing)) {
                return new CorporatePricingStrategy();
            }
            return new IndividualPricingStrategy(false, false);
        }
    }
}
