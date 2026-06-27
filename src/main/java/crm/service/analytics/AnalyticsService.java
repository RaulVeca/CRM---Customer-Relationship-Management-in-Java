package crm.service.analytics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import crm.model.entity.Contact;
import crm.model.entity.Course;
import crm.model.entity.Enrollment;
import crm.model.entity.Opportunity;
import crm.model.enums.EnrollmentStatus;
import crm.model.enums.LeadStatus;
import crm.model.enums.OpportunityStage;
import crm.repository.ContactRepository;
import crm.repository.CourseRepository;
import crm.repository.EnrollmentRepository;
import crm.repository.OpportunityRepository;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SINGLETON - AnalyticsService.
 *
 * <p>Computes the admin analytics shown on the Analytics dashboard:
 * <ul>
 *   <li><b>Demographic data</b> - distribution of contacts by type, experience,
 *       lead source, county and industry.</li>
 *   <li><b>Churn rate</b> - lead churn, enrollment drop-out and B2B opportunity
 *       loss rate, derived from the existing status fields.</li>
 *   <li><b>Click-Through Rate</b> - clicks / impressions for the public course
 *       catalog (see {@link MetricsService}).</li>
 * </ul>
 */
public class AnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);
    private static volatile AnalyticsService instance;

    private static final int TOP_N = 8;

    private final ContactRepository contactRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final OpportunityRepository opportunityRepository;
    private final CourseRepository courseRepository;
    private final MetricsService metricsService;

    private AnalyticsService() {
        this.contactRepository = ContactRepository.getInstance();
        this.enrollmentRepository = EnrollmentRepository.getInstance();
        this.opportunityRepository = OpportunityRepository.getInstance();
        this.courseRepository = CourseRepository.getInstance();
        this.metricsService = MetricsService.getInstance();
    }

    public static AnalyticsService getInstance() {
        if (instance == null) {
            synchronized (AnalyticsService.class) {
                if (instance == null) {
                    instance = new AnalyticsService();
                }
            }
        }
        return instance;
    }

    public Analytics getAnalytics() {
        return new Analytics(demographics(), churn(), ctr());
    }

    // ---------------------------------------------------------------
    // Demographic data
    // ---------------------------------------------------------------

    private Demographics demographics() {
        List<Contact> contacts = contactRepository.findAll();

        Map<String, Long> byType = new LinkedHashMap<>();
        Map<String, Long> byExperience = new LinkedHashMap<>();
        Map<String, Long> byLeadSource = new LinkedHashMap<>();
        Map<String, Long> byCounty = new LinkedHashMap<>();
        Map<String, Long> byIndustry = new LinkedHashMap<>();

        for (Contact c : contacts) {
            inc(byType, c.getContactType() == null ? "Unknown" : c.getContactType().name());
            inc(byExperience, c.getExperienceLevel() == null ? "Unknown" : c.getExperienceLevel().name());
            inc(byLeadSource, c.getLeadSource() == null ? "Unknown" : c.getLeadSource().name());
            inc(byCounty, blankOr(c.getAddressCounty(), "Unknown"));
            if (c.getIndustry() != null && !c.getIndustry().isBlank()) {
                inc(byIndustry, c.getIndustry());
            }
        }

        return new Demographics(
                contacts.size(),
                byType,
                byExperience,
                byLeadSource,
                topN(byCounty),
                topN(byIndustry));
    }

    // ---------------------------------------------------------------
    // Churn rate
    // ---------------------------------------------------------------

    private Churn churn() {
        long lostLeads = 0;
        long enrolledLeads = 0;
        for (Contact c : contactRepository.findAll()) {
            if (c.getLeadStatus() == LeadStatus.LOST) lostLeads++;
            else if (c.getLeadStatus() == LeadStatus.ENROLLED) enrolledLeads++;
        }
        double leadChurnRate = pct(lostLeads, lostLeads + enrolledLeads);

        List<Enrollment> enrollments = enrollmentRepository.findAll();
        long droppedEnrollments = enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.DROPPED
                        || e.getStatus() == EnrollmentStatus.CANCELLED)
                .count();
        double enrollmentDropoutRate = pct(droppedEnrollments, enrollments.size());

        long wonOpps = 0;
        long lostOpps = 0;
        for (Opportunity o : opportunityRepository.findAll()) {
            if (o.getStage() == OpportunityStage.WON) wonOpps++;
            else if (o.getStage() == OpportunityStage.LOST) lostOpps++;
        }
        double opportunityLossRate = pct(lostOpps, wonOpps + lostOpps);

        return new Churn(
                leadChurnRate, lostLeads, enrolledLeads,
                enrollmentDropoutRate, droppedEnrollments, enrollments.size(),
                opportunityLossRate, lostOpps, wonOpps);
    }

    // ---------------------------------------------------------------
    // Click-Through Rate
    // ---------------------------------------------------------------

    private Ctr ctr() {
        Map<Long, long[]> counts = metricsService.getAllCounts();
        Map<Long, String> names = new LinkedHashMap<>();
        for (Course c : courseRepository.findAll()) {
            names.put(c.getId(), c.getName());
        }

        long totalImpressions = 0;
        long totalClicks = 0;
        List<CtrRow> rows = new java.util.ArrayList<>();
        for (Map.Entry<Long, long[]> e : counts.entrySet()) {
            long impressions = e.getValue()[0];
            long clicks = e.getValue()[1];
            totalImpressions += impressions;
            totalClicks += clicks;
            rows.add(new CtrRow(
                    e.getKey(),
                    names.getOrDefault(e.getKey(), "(course " + e.getKey() + ")"),
                    impressions,
                    clicks,
                    pct(clicks, impressions)));
        }
        rows.sort(Comparator.comparingLong(CtrRow::impressions).reversed());

        double overallCtr = pct(totalClicks, totalImpressions);
        return new Ctr(overallCtr, totalImpressions, totalClicks, rows);
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private static void inc(Map<String, Long> map, String key) {
        map.merge(key, 1L, Long::sum);
    }

    private static String blankOr(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    /** Keep the largest TOP_N entries, ordered descending. */
    private static Map<String, Long> topN(Map<String, Long> map) {
        Map<String, Long> out = new LinkedHashMap<>();
        map.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(TOP_N)
                .forEach(e -> out.put(e.getKey(), e.getValue()));
        return out;
    }

    /** Percentage of part out of total, rounded to one decimal; 0 if total is 0. */
    private static double pct(long part, long total) {
        if (total <= 0) return 0.0;
        return Math.round((double) part / total * 1000.0) / 10.0;
    }

    // ---------------------------------------------------------------
    // View records
    // ---------------------------------------------------------------

    public record Demographics(
            long totalContacts,
            Map<String, Long> byType,
            Map<String, Long> byExperience,
            Map<String, Long> byLeadSource,
            Map<String, Long> byCounty,
            Map<String, Long> byIndustry) {}

    public record Churn(
            double leadChurnRate, long lostLeads, long enrolledLeads,
            double enrollmentDropoutRate, long droppedEnrollments, long totalEnrollments,
            double opportunityLossRate, long lostOpportunities, long wonOpportunities) {}

    public record CtrRow(long courseId, String courseName, long impressions, long clicks, double ctr) {}

    public record Ctr(double overallCtr, long totalImpressions, long totalClicks, List<CtrRow> courses) {}

    public record Analytics(Demographics demographics, Churn churn, Ctr ctr) {}
}
