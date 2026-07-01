package crm.model.enums;

public enum LeadStatus {
    NEW("New"),
    CONTACTED("Contacted"),
    INTERESTED("Interested"),
    QUALIFIED("Qualified"),
    ENROLLED("Enrolled"),
    LOST("Lost");

    private final String label;

    LeadStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
