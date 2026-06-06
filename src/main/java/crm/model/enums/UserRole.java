package crm.model.enums;

public enum UserRole {
    ADMIN("Administrator"),
    SALES_MANAGER("Sales Manager"),
    SALES_AGENT("Sales Agent"),
    TRAINER("Trainer"),
    SUPPORT("Support"),
    MARKETING("Marketing");

    private final String label;

    UserRole(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
