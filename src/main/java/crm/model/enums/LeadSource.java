package crm.model.enums;

public enum LeadSource {
    WEBSITE("Website"),
    FACEBOOK("Facebook"),
    GOOGLE_ADS("Google Ads"),
    LINKEDIN("LinkedIn"),
    REFERRAL("Recomandare"),
    COLD_CALL("Cold Call"),
    EVENT("Eveniment"),
    EMAIL_CAMPAIGN("Campanie Email"),
    PARTNER("Partener"),
    OTHER("Altul");

    private final String label;

    LeadSource(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
