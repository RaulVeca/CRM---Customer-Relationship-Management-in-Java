package crm.web.dto;

import crm.model.entity.Course;
import crm.service.review.ReviewService.CourseReviewView;

import java.time.format.DateTimeFormatter;

/**
 * Public projection of a single course review, enriched with the course it
 * belongs to. Feeds the marketing site's scrolling reviews ticker, which shows
 * every review across every course regardless of rating or feedback content.
 */
public record PublicReviewDto(
        Long courseId,
        String courseCode,
        String courseName,
        String author,
        int rating,
        String comment,
        String date
) {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static PublicReviewDto from(Course course, CourseReviewView v) {
        return new PublicReviewDto(
                course.getId(),
                course.getCode(),
                course.getName(),
                v.author(),
                v.rating(),
                v.comment(),
                v.date() == null ? null : v.date().format(FMT));
    }
}
