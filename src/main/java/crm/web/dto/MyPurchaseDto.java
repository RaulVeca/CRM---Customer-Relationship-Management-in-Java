package crm.web.dto;

import crm.model.entity.Course;
import crm.service.review.ReviewService.PurchasedCourse;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/**
 * A single course bought by the logged-in contact, for their personal
 * "my courses" page. Carries the course details plus how much was paid, when,
 * and the contact's own rating (if they reviewed it).
 */
public record MyPurchaseDto(
        Long courseId,
        String code,
        String name,
        String description,
        String category,
        String categoryLabel,
        String level,
        Integer durationHours,
        BigDecimal amountPaid,
        String purchaseDate,
        Integer rating
) {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static MyPurchaseDto from(PurchasedCourse p) {
        Course c = p.course();
        return new MyPurchaseDto(
                c.getId(),
                c.getCode(),
                c.getName(),
                c.getDescription(),
                c.getCategory() == null ? null : c.getCategory().name(),
                c.getCategory() == null ? null : c.getCategory().getLabel(),
                c.getLevel() == null ? null : c.getLevel().name(),
                c.getDurationHours(),
                p.amount(),
                p.date() == null ? null : p.date().format(FMT),
                p.rating());
    }
}
