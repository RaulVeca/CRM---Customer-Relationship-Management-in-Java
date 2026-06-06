package crm.model.enums;

public enum OpportunityStage {
    LEAD_QUALIFICATION("Calificare Lead", 10),
    NEEDS_ANALYSIS("Analiză Nevoi", 25),
    PROPOSAL_SENT("Ofertă Trimisă", 50),
    NEGOTIATION("Negociere", 70),
    CONTRACT_REVIEW("Revizuire Contract", 85),
    WON("Câștigat", 100),
    LOST("Pierdut", 0);

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
