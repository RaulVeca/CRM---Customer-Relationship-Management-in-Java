package crm.web.controller;

import crm.facade.CrmFacade;
import crm.service.analytics.AnalyticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin analytics: demographic data, churn rate and click-through rate.
 */
@RestController
@RequestMapping("/api/stats")
public class AnalyticsController {

    private final CrmFacade facade;

    public AnalyticsController(CrmFacade facade) {
        this.facade = facade;
    }

    @GetMapping("/analytics")
    public AnalyticsService.Analytics analytics() {
        return facade.getAnalytics();
    }
}
