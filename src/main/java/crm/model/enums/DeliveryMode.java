package crm.model.enums;

public enum DeliveryMode {
    ONLINE("Online"),
    ON_SITE("On-site"),
    HYBRID("Hybrid"),
    AT_CLIENT("At client");

    private final String label;

    DeliveryMode(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}