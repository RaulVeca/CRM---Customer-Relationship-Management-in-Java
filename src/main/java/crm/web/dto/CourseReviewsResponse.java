package crm.web.dto;

import java.util.List;

/**
 * Aggregated reviews payload for one course: the average rating, how many
 * reviews exist and the individual reviews themselves.
 */
public record CourseReviewsResponse(
        double average,
        int count,
        List<CourseReviewDto> reviews
) {}
