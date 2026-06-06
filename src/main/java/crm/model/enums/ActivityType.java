package crm.model.enums;

public enum ActivityType {
    EMAIL("Email"),
    CALL("Apel"),
    MEETING("Întâlnire"),
    TASK("Task"),
    NOTE("Notă"),
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
