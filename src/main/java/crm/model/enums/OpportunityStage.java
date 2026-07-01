package crm.model.enums;

public enum OpportunityStage {
    LEAD_QUALIFICATION("Lead Qualification", 10),
    NEEDS_ANALYSIS("Needs Analysis", 25),
    PROPOSAL_SENT("Proposal Sent", 50),
    NEGOTIATION("Negotiation", 70),
    CONTRACT_REVIEW("Contract Review", 85),
    WON("Won", 100),
    LOST("Lost", 0);

    private final String label;
    private final int defaultProbability;

    OpportunityStage(String label, int defaultProbability) {
        this.label = label;
        this.defaultProbability = defaultProbability;
    }

    public String getLabel() {
        return label;
    }

    public int getDefaultProbability() {
        return defaultProbability;
    }
}
