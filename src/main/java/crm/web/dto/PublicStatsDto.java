package crm.web.dto;

import crm.service.review.ReviewService.SiteStats;

/**
 * The marketing site's headline figures, computed from live data rather than
 * hard-coded in the page, so they stay true as the catalog grows.
 */
public record PublicStatsDto(
        int courseCount,
        int learnerCount,
        double averageRating,
        int reviewCount
) {
    public static PublicStatsDto from(SiteStats s) {
        return new PublicStatsDto(s.courseCount(), s.learnerCount(), s.averageRating(), s.reviewCount());
    }
}
