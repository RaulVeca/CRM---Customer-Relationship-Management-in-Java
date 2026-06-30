package crm.web.controller;

import crm.facade.CrmFacade;
import crm.model.entity.Enrollment;
import crm.web.dto.PurchaseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for course enrollments.
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

    /** Admin purchase history: every enrollment enriched with course & buyer. */
    @GetMapping("/history")
    public List<PurchaseDto> history() {
        return facade.getPurchaseHistory().stream()
                .map(PurchaseDto::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Enrollment enroll(@RequestBody EnrollRequest req) {
        return facade.enrollContact(req.contactId(), req.sessionId());
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<Void> complete(@PathVariable Long id, @RequestParam java.math.BigDecimal grade) {
        facade.completeCourse(id, grade);
        return ResponseEntity.noContent().build();
    }

    public record EnrollRequest(Long contactId, Long sessionId) {}
}
