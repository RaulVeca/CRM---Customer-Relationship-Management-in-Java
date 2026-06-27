package crm.web.controller;

import crm.facade.CrmFacade;
import crm.model.entity.Contact;
import crm.model.enums.CourseCategory;
import crm.service.review.ReviewService.RatingSummary;
import crm.web.dto.CourseReviewDto;
import crm.web.dto.CourseReviewsResponse;
import crm.web.dto.PublicCompanyDto;
import crm.web.dto.PublicCourseDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * Public, unauthenticated catalog for the marketing website.
 *
 * <p>Lets prospective individual visitors (and investors) browse the available
 * course types and the business activity of the companies the training firm
 * works with - one of the mandatory web-app requirements.</p>
 */
@RestController
@RequestMapping("/api/public")
public class PublicCatalogController {

    private static final int MAX_PUBLIC_CONTACTS = 500;

    private final CrmFacade facade;

    public PublicCatalogController(CrmFacade facade) {
        this.facade = facade;
    }

    @GetMapping("/courses")
    public List<PublicCourseDto> courses(@RequestParam(required = false) CourseCategory category) {
        var source = category != null
                ? facade.getCoursesByCategory(category)
                : facade.getActiveCourses();
        return source.stream()
                .map(c -> {
                    RatingSummary rating = facade.getCourseRating(c.getId());
                    return PublicCourseDto.from(c, rating.average(), rating.count());
                })
                .toList();
    }

    // =====================================================
    // PURCHASE + REVIEWS
    // =====================================================

    /**
     * Records a purchase of a course by an individual visitor (identified by
     * email). This is what unlocks the ability to review the course afterwards.
     */
    @PostMapping("/courses/{courseId}/purchase")
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseResponse purchase(@PathVariable Long courseId, @RequestBody PurchaseRequest req) {
        var enrollment = facade.purchaseCourse(req.email(), req.firstName(), req.lastName(), courseId);
        return new PurchaseResponse(enrollment.getId(), "Course purchased successfully.");
    }

    /** All published reviews for a course plus their aggregate rating. */
    @GetMapping("/courses/{courseId}/reviews")
    public CourseReviewsResponse reviews(@PathVariable Long courseId) {
        RatingSummary summary = facade.getCourseRating(courseId);
        List<CourseReviewDto> reviews = facade.getCourseReviews(courseId).stream()
                .map(CourseReviewDto::from)
                .toList();
        return new CourseReviewsResponse(summary.average(), summary.count(), reviews);
    }

    /**
     * Submits a 1-5 star review for a course. Rejected with 422 unless the
     * client (matched by email) has already purchased the course.
     */
    @PostMapping("/courses/{courseId}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public CourseReviewDto review(@PathVariable Long courseId, @RequestBody ReviewRequest req) {
        int stars = req.rating() == null ? 0 : req.rating();
        facade.reviewCourse(req.email(), courseId, stars, req.comment());
        // return the freshly submitted review (latest first)
        return facade.getCourseReviews(courseId).stream()
                .findFirst()
                .map(CourseReviewDto::from)
                .orElse(new CourseReviewDto("", stars, req.comment(), null));
    }

    public record PurchaseRequest(String email, String firstName, String lastName) {}

    public record PurchaseResponse(Long enrollmentId, String message) {}

    public record ReviewRequest(String email, Integer rating, String comment) {}

    @GetMapping("/categories")
    public List<CategoryDto> categories() {
        return Arrays.stream(CourseCategory.values())
                .map(c -> new CategoryDto(c.name(), c.getLabel()))
                .toList();
    }

    @GetMapping("/companies")
    public List<PublicCompanyDto> companies() {
        return facade.getAllContacts(0, MAX_PUBLIC_CONTACTS).stream()
                .filter(Contact::isCorporate)
                .filter(c -> c.getCompanyName() != null)
                .map(PublicCompanyDto::from)
                .toList();
    }

    public record CategoryDto(String name, String label) {}
}
