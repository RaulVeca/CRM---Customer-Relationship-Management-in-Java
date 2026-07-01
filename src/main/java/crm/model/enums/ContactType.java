package crm.model.enums;

public enum ContactType {
    INDIVIDUAL("Individual"),
    CORPORATE("Company");

    private final String label;

    ContactType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
