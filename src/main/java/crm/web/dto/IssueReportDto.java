package crm.web.dto;

import crm.model.entity.IssueReport;

import java.time.format.DateTimeFormatter;

/**
 * Public projection of a reported issue for the admin portal's Issues view.
 */
public record IssueReportDto(
        Long id,
        String reporterName,
        String reporterEmail,
        String message,
        String status,
        String date
) {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static IssueReportDto from(IssueReport r) {
        return new IssueReportDto(
                r.getId(),
                r.getReporterName(),
                r.getReporterEmail(),
                r.getMessage(),
                r.getStatus(),
                r.getCreatedAt() == null ? null : r.getCreatedAt().format(FMT));
    }
}
