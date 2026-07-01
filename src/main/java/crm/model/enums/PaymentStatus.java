package crm.model.enums;

public enum PaymentStatus {
    UNPAID("Unpaid"),
    PARTIAL("Partial payment"),
    PAID("Paid"),
    REFUNDED("Refunded"),
    OVERDUE("Overdue");

    private final String label;

    PaymentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
