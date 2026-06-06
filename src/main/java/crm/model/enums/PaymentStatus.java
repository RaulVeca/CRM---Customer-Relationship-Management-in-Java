package crm.model.enums;

public enum PaymentStatus {
    UNPAID("Neplătit"),
    PARTIAL("Plată parțială"),
    PAID("Plătit"),
    REFUNDED("Returnat"),
    OVERDUE("Întârziat");

    private final String label;

    PaymentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
