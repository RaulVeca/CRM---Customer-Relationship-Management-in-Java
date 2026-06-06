package crm.model.enums;

public enum EnrollmentStatus {
    PENDING("În așteptare"),
    CONFIRMED("Confirmat"),
    ATTENDING("În participare"),
    COMPLETED("Finalizat"),
    DROPPED("Abandonat"),
    CANCELLED("Anulat");

    private final String label;

    EnrollmentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
