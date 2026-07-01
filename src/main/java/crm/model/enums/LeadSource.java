package crm.model.enums;

public enum LeadSource {
    WEBSITE("Website"),
    FACEBOOK("Facebook"),
    GOOGLE_ADS("Google Ads"),
    LINKEDIN("LinkedIn"),
    REFERRAL("Referral"),
    COLD_CALL("Cold Call"),
    EVENT("Event"),
    EMAIL_CAMPAIGN("Email Campaign"),
    PARTNER("Partner"),
    OTHER("Other");

    private final String label;

    LeadSource(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
