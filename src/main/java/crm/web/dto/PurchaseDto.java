package crm.web.dto;

import crm.service.enrollment.PurchaseHistoryService.PurchaseRecord;

import java.time.format.DateTimeFormatter;

/**
 * Admin-facing projection of a single purchase (enrollment) for the purchase
 * history table.
 */
public record PurchaseDto(
        Long enrollmentId,
        String studentName,
        String studentEmail,
        String courseName,
        String courseCode,
        String status,
        String date,
        Integer rating
) {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static PurchaseDto from(PurchaseRecord r) {
        return new PurchaseDto(
                r.enrollmentId(),
                r.studentName(),
                r.studentEmail(),
                r.courseName(),
                r.courseCode(),
                r.status(),
                r.date() == null ? null : r.date().format(FMT),
                r.rating());
    }
}
