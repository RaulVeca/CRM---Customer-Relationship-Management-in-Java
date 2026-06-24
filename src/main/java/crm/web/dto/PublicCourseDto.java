package crm.web.dto;

import crm.model.entity.Course;

import java.math.BigDecimal;

/**
 * Public, read-only projection of a {@link Course} for the marketing site.
 * Hides internal pricing/configuration fields the public should not see.
 */
public record PublicCourseDto(
        Long id,
        String code,
        String name,
        String description,
        String category,
        String categoryLabel,
        String level,
        Integer durationHours,
        BigDecimal priceIndividual
) {
    public static PublicCourseDto from(Course c) {
        return new PublicCourseDto(
                c.getId(),
                c.getCode(),
                c.getName(),
                c.getDescription(),
                c.getCategory() == null ? null : c.getCategory().name(),
                c.getCategory() == null ? null : c.getCategory().getLabel(),
                c.getLevel() == null ? null : c.getLevel().name(),
                c.getDurationHours(),
                c.getPriceIndividual()
        );
    }
}
