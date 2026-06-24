package crm.model.enums;

public enum AuctionStatus {
    OPEN("Open"),
    AWARDED("Awarded"),
    CANCELLED("Cancelled");

    private final String label;

    AuctionStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
