package crm.service.support;

import crm.dao.IssueReportDao;
import crm.exception.ResourceNotFoundException;
import crm.model.entity.IssueReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;

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

    /** A report is OPEN until an admin marks it SOLVED; both ways are allowed. */
    public static final String STATUS_OPEN = "OPEN";
    public static final String STATUS_SOLVED = "SOLVED";

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

    /**
     * Moves a report between {@link #STATUS_OPEN} and {@link #STATUS_SOLVED} —
     * the admin Issues view toggles it both ways, so a report marked solved by
     * mistake can be reopened.
     *
     * @param status the target status, case-insensitive
     * @throws IllegalArgumentException if the status is neither OPEN nor SOLVED
     * @throws ResourceNotFoundException if no report has that id
     */
    public IssueReport changeStatus(Long id, String status) {
        String target = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
        if (!STATUS_OPEN.equals(target) && !STATUS_SOLVED.equals(target)) {
            throw new IllegalArgumentException(
                    "Unknown issue status: " + status + " (expected " + STATUS_OPEN + " or " + STATUS_SOLVED + ").");
        }
        IssueReport report = issueReportDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue report", id));
        report.setStatus(target);
        IssueReport saved = issueReportDao.update(report);
        logger.info("Issue report status changed: id={}, status={}", id, target);
        return saved;
    }

    /**
     * Permanently removes a single report once an admin has dealt with it.
     *
     * @return {@code true} if a report with that id existed and was removed
     */
    public boolean delete(Long id) {
        boolean removed = issueReportDao.deleteById(id);
        logger.info("Issue report deleted: id={}, existed={}", id, removed);
        return removed;
    }

    /**
     * Permanently removes every report — the admin Issues view's "Delete all"
     * action. There is no undo, so the portal confirms before calling this.
     *
     * @return how many reports were removed
     */
    public int deleteAll() {
        int removed = issueReportDao.deleteAll();
        logger.info("All issue reports deleted: {} rows", removed);
        return removed;
    }

    private static String blankToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
