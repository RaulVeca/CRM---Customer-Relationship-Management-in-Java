package crm.web.controller;

import crm.facade.CrmFacade;
import crm.model.entity.Contact;
import crm.model.enums.CourseCategory;
import crm.service.review.ReviewService.RatingSummary;
import crm.web.dto.CourseReviewDto;
import crm.web.dto.CourseReviewsResponse;
import crm.web.dto.MyPurchaseDto;
import crm.web.dto.PublicCompanyDto;
import crm.web.dto.PublicCourseDto;
import crm.web.dto.PublicReviewDto;
import crm.web.dto.PublicStatsDto;
import crm.web.stats.PublicStatsBroadcaster;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Arrays;
import java.util.Comparator;
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
    private final PublicStatsBroadcaster statsBroadcaster;

    public PublicCatalogController(CrmFacade facade, PublicStatsBroadcaster statsBroadcaster) {
        this.facade = facade;
        this.statsBroadcaster = statsBroadcaster;
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

    /**
     * The courses already bought by the logged-in contact (matched by email).
     * Feeds their personal "my courses" page and the catalog's "already bought"
     * button state, so the two roles stay personal: each contact only ever sees
     * its own purchases.
     */
    @GetMapping("/me/purchases")
    public List<MyPurchaseDto> myPurchases(@RequestParam String email) {
        return facade.getPurchasedCourses(email).stream()
                .map(MyPurchaseDto::from)
                .toList();
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
     * Published reviews across every active course, newest first, each tagged
     * with the course it belongs to. Feeds the marketing site's continuously
     * scrolling reviews ticker. Only reviews that carry BOTH a real rating
     * (at least 1 star) AND written feedback (at least 2 characters) are shown,
     * so empty/placeholder entries never reach the bar.
     */
    @GetMapping("/reviews")
    public List<PublicReviewDto> allReviews() {
        return facade.getActiveCourses().stream()
                .flatMap(course -> facade.getCourseReviews(course.getId()).stream()
                        .filter(view -> view.rating() >= 1
                                && view.comment() != null
                                && view.comment().trim().length() >= 2)
                        .map(view -> PublicReviewDto.from(course, view)))
                .sorted(Comparator.comparing(
                        PublicReviewDto::date,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
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

    // =====================================================
    // CLICK-THROUGH TRACKING (feeds the admin CTR metric)
    // =====================================================

    /** Records that these courses were shown in the catalog (impressions). */
    @PostMapping("/metrics/impressions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void impressions(@RequestBody ImpressionsRequest req) {
        facade.recordCourseImpressions(req.courseIds());
    }

    /** Records that a visitor clicked through on a course (click). */
    @PostMapping("/courses/{courseId}/click")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void click(@PathVariable Long courseId) {
        facade.recordCourseClick(courseId);
    }

    public record PurchaseRequest(String email, String firstName, String lastName) {}

    public record PurchaseResponse(Long enrollmentId, String message) {}

    public record ReviewRequest(String email, Integer rating, String comment) {}

    public record ImpressionsRequest(List<Long> courseIds) {}

    /**
     * The real headline figures for the marketing site: active courses,
     * distinct learners enrolled on them, and the average review score. Served
     * live so the numbers on the landing page can never drift from the data.
     */
    @GetMapping("/stats")
    public PublicStatsDto stats() {
        return PublicStatsDto.from(facade.getSiteStats());
    }

    /**
     * The same figures as {@link #stats()}, but streamed: the current values
     * arrive on connect and a fresh "stats" event is pushed whenever they
     * change, so an open landing page stays live without polling.
     */
    @GetMapping(path = "/stats/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter statsStream() {
        return statsBroadcaster.subscribe();
    }

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
