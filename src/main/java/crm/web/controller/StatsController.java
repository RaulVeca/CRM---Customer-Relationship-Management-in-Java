package crm.web.controller;

import crm.facade.CrmFacade;
import crm.model.enums.LeadStatus;
import crm.model.enums.OpportunityStage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Aggregated metrics for the internal admin dashboard.
 */
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final CrmFacade facade;

    public StatsController(CrmFacade facade) {
        this.facade = facade;
    }

    @GetMapping("/dashboard")
    public DashboardStats dashboard() {
        Map<String, Long> contactsByStatus = new LinkedHashMap<>();
        for (LeadStatus status : LeadStatus.values()) {
            contactsByStatus.put(status.name(), facade.getContactsByStatus(status));
        }

        Map<String, Integer> pipelineByStage = new LinkedHashMap<>();
        for (OpportunityStage stage : OpportunityStage.values()) {
            pipelineByStage.put(stage.name(), facade.getOpportunitiesByStage(stage).size());
        }

        BigDecimal weightedPipeline = facade.getActivePipeline().stream()
                .map(o -> o.getWeightedValue() == null ? BigDecimal.ZERO : o.getWeightedValue())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DashboardStats(
                facade.getTotalContacts(),
                facade.getHotLeads(100).size(),
                facade.getActivePipeline().size(),
                weightedPipeline,
                contactsByStatus,
                pipelineByStage
        );
    }

    public record DashboardStats(
            long totalContacts,
            int hotLeads,
            int activeOpportunities,
            BigDecimal weightedPipelineValue,
            Map<String, Long> contactsByStatus,
            Map<String, Integer> pipelineByStage
    ) {}
}
