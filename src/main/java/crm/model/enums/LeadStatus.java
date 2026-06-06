package crm.model.enums;

public enum LeadStatus {
    NEW("Nou"),
    CONTACTED("Contactat"),
    INTERESTED("Interesat"),
    QUALIFIED("Calificat"),
    ENROLLED("Înscris"),
    LOST("Pierdut");

    private final String label;

    LeadStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
