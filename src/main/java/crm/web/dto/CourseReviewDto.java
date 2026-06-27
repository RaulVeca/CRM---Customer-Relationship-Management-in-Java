package crm.web.dto;

import crm.service.review.ReviewService.CourseReviewView;

import java.time.format.DateTimeFormatter;

/**
 * Public projection of a single course review for the marketing site.
 */
public record CourseReviewDto(
        String author,
        int rating,
        String comment,
        String date
) {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static CourseReviewDto from(CourseReviewView v) {
        return new CourseReviewDto(
                v.author(),
                v.rating(),
                v.comment(),
                v.date() == null ? null : v.date().format(FMT));
    }
}
