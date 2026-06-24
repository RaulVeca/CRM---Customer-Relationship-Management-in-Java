package crm.web.controller;

import crm.facade.CrmFacade;
import crm.model.entity.Activity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for activities (calls, meetings, follow-ups) logged against
 * contacts and opportunities.
 */
@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final CrmFacade facade;

    public ActivityController(CrmFacade facade) {
        this.facade = facade;
    }

    @GetMapping("/by-contact/{contactId}")
    public List<Activity> byContact(@PathVariable Long contactId) {
        return facade.getActivitiesForContact(contactId);
    }

    @GetMapping("/upcoming")
    public List<Activity> upcoming(@RequestParam Long userId,
                                   @RequestParam(defaultValue = "7") int days) {
        return facade.getUpcomingActivities(userId, days);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Activity create(@RequestBody Activity activity) {
        return facade.logActivity(activity);
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<Void> complete(@PathVariable Long id, @RequestBody CompleteRequest req) {
        facade.completeActivity(id, req.outcome(), req.nextSteps());
        return ResponseEntity.noContent().build();
    }

    public record CompleteRequest(String outcome, String nextSteps) {}
}
