package crm.model.enums;

public enum ContactType {
    INDIVIDUAL("Persoană fizică"),
    CORPORATE("Companie");

    private final String label;

    ContactType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
