package crm.web.controller;

import crm.facade.CrmFacade;
import crm.web.dto.IssueReportDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Issue reporting: clients submit problems from the portal's "Report an issue"
 * window ({@code POST}), and admins read every reported issue in the admin
 * portal's Issues view ({@code GET}).
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

    public record SubmitRequest(String email, String name, String message) {}
}
