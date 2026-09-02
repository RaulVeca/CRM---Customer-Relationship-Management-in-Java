package crm.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import crm.model.entity.Contact;
import crm.model.enums.ContactType;

/**
 * STRATEGY PATTERN - Context
 * 
 * Selectează strategia potrivită pentru tipul de contact.
 * Client-ul folosește acest Context fără să știe ce strategie e folosită.
 */
public class LeadScoringContext {

    private static final Logger logger = LoggerFactory.getLogger(LeadScoringContext.class);

    private LeadScoringStrategy strategy;

    public LeadScoringContext(LeadScoringStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Constructor implicit care alege strategia bazată pe tipul de contact.
     */
    public LeadScoringContext(Contact contact) {
        this.strategy = selectStrategy(contact);
    }

    public int calculateInitialScore(Contact contact) {
        int score = strategy.calculateInitialScore(contact);
        logger.debug("Initial score for contact {} (strategy: {}): {}",
            contact.getEmail(), strategy.getStrategyName(), score);
        return score;
    }

    public int calculateActivityImpact(String activityType) {
        return strategy.calculateActivityImpact(activityType);
    }

    /**
     * Selectează strategia pe baza tipului de contact.
     */
    private static LeadScoringStrategy selectStrategy(Contact contact) {
        if (contact.getContactType() == ContactType.CORPORATE) {
            return new B2BLeadScoringStrategy();
        }
        return new SimpleLeadScoringStrategy();
    }
}
