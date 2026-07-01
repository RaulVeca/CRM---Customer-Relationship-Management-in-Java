package crm.model.enums;

public enum SessionStatus {
    PLANNED("Planned"),
    OPEN_ENROLLMENT("Open enrollment"),
    FULL("Full"),
    ACTIVE("In progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String label;

    SessionStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
