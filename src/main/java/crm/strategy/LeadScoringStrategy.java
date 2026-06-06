package crm.strategy;

import crm.model.entity.Contact;

/**
 * STRATEGY PATTERN - Lead Scoring
 * 
 * Permite folosirea de algoritmi diferiți pentru calcul scor lead,
 * interschimbabili în runtime fără modificarea codului client.
 * 
 * Implementări disponibile:
 * - SimpleLeadScoringStrategy: scoring de bază
 * - AdvancedLeadScoringStrategy: scoring complex bazat pe comportament
 * - B2BLeadScoringStrategy: scoring specific clienților corporate
 */
public interface LeadScoringStrategy {

    /**
     * Calculează scorul inițial al unui lead la creare.
     */
    int calculateInitialScore(Contact contact);

    /**
     * Calculează modificarea scorului pe baza unei activități.
     */
    int calculateActivityImpact(String activityType);

    /**
     * Returnează numele strategiei (pentru logging).
     */
    String getStrategyName();
}
