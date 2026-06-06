package crm.strategy;

import crm.model.entity.Contact;

/**
 * Strategie specifică pentru lead-uri B2B (corporate).
 * Pune accent pe factori specifici companiilor:
 * - Mărimea companiei
 * - Industria
 * - Datele fiscale (validare seriozitate)
 */
public class B2BLeadScoringStrategy implements LeadScoringStrategy {

    @Override
    public int calculateInitialScore(Contact contact) {
        int score = 0;

        // Datele de bază
        if (contact.getEmail() != null && !contact.getEmail().isEmpty()) score += 5;
        if (contact.getPhone() != null && !contact.getPhone().isEmpty()) score += 10;
        if (contact.getCompanyName() != null && !contact.getCompanyName().isEmpty()) score += 10;

        // Date fiscale (companie serioasă)
        if (contact.getFiscalCode() != null && !contact.getFiscalCode().isEmpty()) score += 15;
        if (contact.getRegistrationNumber() != null && !contact.getRegistrationNumber().isEmpty()) score += 10;

        // Industria - anumite industrii au scor mai mare
        if (contact.getIndustry() != null) {
            String industry = contact.getIndustry().toLowerCase();
            if (industry.contains("it") || industry.contains("software") 
                    || industry.contains("tech")) {
                score += 20;
            } else if (industry.contains("financ") || industry.contains("bank")) {
                score += 18;
            } else if (industry.contains("telecom") || industry.contains("consult")) {
                score += 15;
            } else {
                score += 10;
            }
        }

        // Mărimea companiei
        if (contact.getEmployeeCount() != null) {
            int count = contact.getEmployeeCount();
            if (count >= 1000) score += 25;
            else if (count >= 250) score += 20;
            else if (count >= 50) score += 15;
            else if (count >= 10) score += 10;
            else score += 5;
        }

        // Adresă completă
        if (contact.getAddressCity() != null && !contact.getAddressCity().isEmpty()) score += 5;

        return Math.max(0, Math.min(100, score));
    }

    @Override
    public int calculateActivityImpact(String activityType) {
        switch (activityType) {
            case "RFP_RECEIVED":         return 60;  // Request for Proposal
            case "TENDER_INVITATION":    return 50;
            case "MEETING_ATTENDED":     return 40;
            case "PROPOSAL_VIEWED":      return 35;
            case "DECISION_MAKER_CONTACT": return 45;
            case "DEMO_REQUESTED":       return 40;
            case "BUDGET_DISCUSSED":     return 50;
            case "MEETING_SCHEDULED":    return 25;
            case "EMAIL_OPENED":         return 5;
            case "NO_RESPONSE":          return -20;
            case "DECLINED":             return -50;
            default:                     return 0;
        }
    }

    @Override
    public String getStrategyName() {
        return "B2BLeadScoring";
    }
}
