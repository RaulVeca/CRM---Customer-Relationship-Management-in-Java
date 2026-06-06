package crm.model.enums;

public enum SessionStatus {
    PLANNED("Planificat"),
    OPEN_ENROLLMENT("Înscrieri deschise"),
    FULL("Locuri ocupate"),
    ACTIVE("În desfășurare"),
    COMPLETED("Finalizat"),
    CANCELLED("Anulat");

    private final String label;

    SessionStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
