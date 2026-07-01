package crm.builder;

import crm.model.entity.Opportunity;
import crm.model.enums.DeliveryMode;
import crm.model.enums.OpportunityStage;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * BUILDER PATTERN - OpportunityBuilder pentru oportunități B2B.
 */
public class OpportunityBuilder {

    private final Opportunity opp;

    public OpportunityBuilder() {
        this.opp = new Opportunity();
        this.opp.setStage(OpportunityStage.LEAD_QUALIFICATION);
        this.opp.setProbabilityPercent(OpportunityStage.LEAD_QUALIFICATION.getDefaultProbability());
    }

    public OpportunityBuilder forClient(Long clientId) {
        opp.setClientId(clientId);
        return this;
    }

    public OpportunityBuilder title(String title) {
        opp.setTitle(title);
        return this;
    }

    public OpportunityBuilder description(String description) {
        opp.setDescription(description);
        return this;
    }

    public OpportunityBuilder participants(int count) {
        opp.setEstimatedParticipants(count);
        return this;
    }

    public OpportunityBuilder customRequirements(String requirements) {
        opp.setCustomRequirements(requirements);
        return this;
    }

    public OpportunityBuilder deliveryMode(DeliveryMode mode) {
        opp.setDeliveryMode(mode);
        return this;
    }

    public OpportunityBuilder location(String location) {
        opp.setPreferredLocation(location);
        return this;
    }

    public OpportunityBuilder desiredStartDate(LocalDate date) {
        opp.setDesiredStartDate(date);
        return this;
    }

    public OpportunityBuilder estimatedValue(BigDecimal value) {
        opp.setEstimatedValue(value);
        return this;
    }

    public OpportunityBuilder quotedValue(BigDecimal value) {
        opp.setQuotedValue(value);
        return this;
    }

    public OpportunityBuilder probability(int percent) {
        opp.setProbabilityPercent(Math.max(0, Math.min(100, percent)));
        return this;
    }

    public OpportunityBuilder stage(OpportunityStage stage) {
        opp.setStage(stage);
        opp.setProbabilityPercent(stage.getDefaultProbability());
        return this;
    }

    public OpportunityBuilder expectedClose(LocalDate date) {
        opp.setExpectedCloseDate(date);
        return this;
    }

    public OpportunityBuilder assignedTo(Long userId) {
        opp.setAssignedTo(userId);
        return this;
    }

    public OpportunityBuilder competitors(String competitors) {
        opp.setCompetitors(competitors);
        return this;
    }

    public Opportunity build() {
        validate();
        return opp;
    }

    private void validate() {
        if (opp.getClientId() == null) {
            throw new IllegalStateException("Client ID is required");
        }
        if (opp.getTitle() == null || opp.getTitle().isEmpty()) {
            throw new IllegalStateException("The opportunity title is required");
        }
    }
}
