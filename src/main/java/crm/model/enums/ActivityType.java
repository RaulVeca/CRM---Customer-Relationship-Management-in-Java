package crm.model.enums;

public enum ActivityType {
    EMAIL("Email"),
    CALL("Call"),
    MEETING("Meeting"),
    TASK("Task"),
    NOTE("Note"),
    SMS("SMS"),
    DEMO("Demo");

    private final String label;

    ActivityType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
