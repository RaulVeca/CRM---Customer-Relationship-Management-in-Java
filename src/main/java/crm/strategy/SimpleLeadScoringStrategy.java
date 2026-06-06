package crm.strategy;

import crm.model.entity.Contact;
import crm.model.enums.LeadSource;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Strategie simplă pentru lead scoring B2C.
 * Bazată pe completitudinea datelor și sursa lead-ului.
 */
public class SimpleLeadScoringStrategy implements LeadScoringStrategy {

    private static final Map<LeadSource, Integer> SOURCE_SCORES = new HashMap<>();
    private static final Map<String, Integer> ACTIVITY_SCORES = new HashMap<>();

    static {
        SOURCE_SCORES.put(LeadSource.REFERRAL, 35);
        SOURCE_SCORES.put(LeadSource.EVENT, 30);
        SOURCE_SCORES.put(LeadSource.WEBSITE, 25);
        SOURCE_SCORES.put(LeadSource.LINKEDIN, 20);
        SOURCE_SCORES.put(LeadSource.GOOGLE_ADS, 15);
        SOURCE_SCORES.put(LeadSource.FACEBOOK, 10);
        SOURCE_SCORES.put(LeadSource.COLD_CALL, 5);
        SOURCE_SCORES.put(LeadSource.EMAIL_CAMPAIGN, 10);

        ACTIVITY_SCORES.put("EMAIL_OPENED", 15);
        ACTIVITY_SCORES.put("EMAIL_CLICKED", 30);
        ACTIVITY_SCORES.put("SYLLABUS_DOWNLOADED", 30);
        ACTIVITY_SCORES.put("CONSULTATION_REQUESTED", 40);
        ACTIVITY_SCORES.put("CALLBACK_REQUESTED", 50);
        ACTIVITY_SCORES.put("WEBINAR_ATTENDED", 25);
        ACTIVITY_SCORES.put("MEETING_SCHEDULED", 30);
        ACTIVITY_SCORES.put("MEETING_ATTENDED", 40);
        ACTIVITY_SCORES.put("NO_RESPONSE_EMAIL", -20);
        ACTIVITY_SCORES.put("MEETING_CANCELLED", -30);
        ACTIVITY_SCORES.put("UNSUBSCRIBED", -50);
    }

    @Override
    public int calculateInitialScore(Contact contact) {
        int score = 0;

        // Email completat
        score += Optional.ofNullable(contact.getEmail())
                .filter(e -> !e.isEmpty())
                .map(e -> 10).orElse(0);

        // Telefon completat
        score += Optional.ofNullable(contact.getPhone())
                .filter(p -> !p.isEmpty())
                .map(p -> 15).orElse(0);

        // Nume și prenume
        if (contact.getFirstName() != null && !contact.getFirstName().isEmpty()) score += 5;
        if (contact.getLastName() != null && !contact.getLastName().isEmpty()) score += 5;

        // Sursă lead
        score += Optional.ofNullable(contact.getLeadSource())
                .map(SOURCE_SCORES::get)
                .orElse(0);

        // Experiență
        if (contact.getExperienceLevel() != null) {
            switch (contact.getExperienceLevel()) {
                case ADVANCED: score += 20; break;
                case INTERMEDIATE: score += 15; break;
                case BEGINNER: score += 10; break;
            }
        }

        // Obiectiv învățare
        if (contact.getLearningGoal() != null && !contact.getLearningGoal().isEmpty()) {
            score += 10;
            if (contact.getLearningGoal().toLowerCase().contains("reconvers")) score += 15;
            if (contact.getLearningGoal().toLowerCase().contains("career")) score += 10;
        }

        // Consimțământ GDPR și marketing
        if (Boolean.TRUE.equals(contact.getGdprConsent())) score += 5;
        if (Boolean.TRUE.equals(contact.getMarketingConsent())) score += 10;

        return Math.max(0, Math.min(100, score));
    }

    @Override
    public int calculateActivityImpact(String activityType) {
        return ACTIVITY_SCORES.getOrDefault(activityType, 0);
    }

    @Override
    public String getStrategyName() {
        return "SimpleLeadScoring";
    }
}
