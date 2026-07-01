package crm.model.enums;

public enum EnrollmentStatus {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    ATTENDING("Attending"),
    COMPLETED("Completed"),
    DROPPED("Dropped"),
    CANCELLED("Cancelled");

    private final String label;

    EnrollmentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
