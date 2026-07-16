package crm.service.support;

import crm.dao.IssueReportDao;
import crm.model.entity.IssueReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * SINGLETON - IssueReportService.
 *
 * <p>Handles the public "Report an issue" flow: a signed-in client submits a
 * free-text problem description from the portal, which is stored for admins to
 * review. There is no per-request authentication on the site, so the reporter is
 * identified only by the email/name carried on their browser session.</p>
 */
public class IssueReportService {

    private static final Logger logger = LoggerFactory.getLogger(IssueReportService.class);
    private static volatile IssueReportService instance;

    private static final String STATUS_OPEN = "OPEN";

    private final IssueReportDao issueReportDao;

    private IssueReportService() {
        this.issueReportDao = IssueReportDao.getInstance();
    }

    public static IssueReportService getInstance() {
        if (instance == null) {
            synchronized (IssueReportService.class) {
                if (instance == null) {
                    instance = new IssueReportService();
                }
            }
        }
        return instance;
    }

    /**
     * Stores a new issue report. The message is required; the reporter's email and
     * name are optional (kept for context but never trusted for authorization).
     */
    public IssueReport report(String email, String name, String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Please describe the issue before sending.");
        }
        IssueReport report = IssueReport.builder()
                .reporterEmail(blankToNull(email))
                .reporterName(blankToNull(name))
                .message(message.trim())
                .status(STATUS_OPEN)
                .build();
        IssueReport saved = issueReportDao.save(report);
        logger.info("Issue reported: id={}, reporter={}", saved.getId(), saved.getReporterEmail());
        return saved;
    }

    /** All reported issues, newest first (the DAO orders by id descending). */
    public List<IssueReport> getAll() {
        return issueReportDao.findAll();
    }

    private static String blankToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
