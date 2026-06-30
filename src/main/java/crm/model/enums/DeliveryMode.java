package crm.model.enums;

public enum DeliveryMode {
    ONLINE("Online"),
    ON_SITE("La fața locului"),
    HYBRID("Hibrid"),
    AT_CLIENT("La client");

    private final String label;

    DeliveryMode(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}