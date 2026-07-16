package crm.web.controller;

import crm.facade.CrmFacade;
import crm.web.dto.IssueReportDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Issue reporting: clients submit problems from the portal's "Report an issue"
 * window ({@code POST}), and admins read every reported issue in the admin
 * portal's Issues view ({@code GET}), mark them solved or reopen them
 * ({@code PATCH}) and clear them once handled ({@code DELETE}, one report or
 * the whole list).
 */
@RestController
@RequestMapping("/api/issues")
public class IssueReportController {

    private final CrmFacade facade;

    public IssueReportController(CrmFacade facade) {
        this.facade = facade;
    }

    /** Records a new issue reported by a client. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IssueReportDto submit(@RequestBody SubmitRequest req) {
        return IssueReportDto.from(facade.reportIssue(req.email(), req.name(), req.message()));
    }

    /** Every reported issue, newest first — feeds the admin Issues view. */
    @GetMapping
    public List<IssueReportDto> all() {
        return facade.getIssueReports().stream()
                .map(IssueReportDto::from)
                .toList();
    }

    /**
     * Marks a report OPEN or SOLVED. The admin view toggles both ways, so a
     * report marked solved by mistake can be reopened.
     */
    @PatchMapping("/{id}/status")
    public IssueReportDto changeStatus(@PathVariable Long id, @RequestParam String status) {
        return IssueReportDto.from(facade.changeIssueReportStatus(id, status));
    }

    /** Removes one reported issue once an admin has handled it. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return facade.deleteIssueReport(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    /** Clears the whole Issues list, reporting how many reports were removed. */
    @DeleteMapping
    public DeleteAllResponse deleteAll() {
        return new DeleteAllResponse(facade.deleteAllIssueReports());
    }

    public record SubmitRequest(String email, String name, String message) {}

    public record DeleteAllResponse(int deleted) {}
}
