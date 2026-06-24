package crm.web.controller;

import crm.facade.CrmFacade;
import crm.model.entity.Opportunity;
import crm.model.enums.OpportunityStage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for the B2B sales pipeline (opportunities).
 */
@RestController
@RequestMapping("/api/opportunities")
public class OpportunityController {

    private final CrmFacade facade;

    public OpportunityController(CrmFacade facade) {
        this.facade = facade;
    }

    @GetMapping
    public List<Opportunity> list(@RequestParam(required = false) OpportunityStage stage,
                                  @RequestParam(required = false) Long clientId) {
        if (stage != null) {
            return facade.getOpportunitiesByStage(stage);
        }
        if (clientId != null) {
            return facade.getOpportunitiesForClient(clientId);
        }
        return facade.getActivePipeline();
    }

    @GetMapping("/{id}")
    public Opportunity getById(@PathVariable Long id) {
        return facade.getOpportunity(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Opportunity create(@RequestBody Opportunity opportunity) {
        return facade.createOpportunity(opportunity);
    }

    @PatchMapping("/{id}/stage")
    public ResponseEntity<Void> moveStage(@PathVariable Long id, @RequestParam OpportunityStage stage) {
        facade.moveOpportunityStage(id, stage);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/lost")
    public ResponseEntity<Void> markLost(@PathVariable Long id, @RequestParam String reason) {
        facade.markOpportunityAsLost(id, reason);
        return ResponseEntity.noContent().build();
    }
}
