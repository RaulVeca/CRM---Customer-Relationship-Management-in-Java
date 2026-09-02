package crm.model.enums;

/**
 * Professional profile areas used to describe what an employee works in
 * (their work profile) and what they want to learn (their interest profiles).
 */
public enum ProfileArea {
    MACHINE_LEARNING("Machine Learning"),
    ARTIFICIAL_INTELLIGENCE("Artificial Intelligence"),
    DATA_SCIENCE("Data Science"),
    SOFTWARE_DEVELOPMENT("Software Development"),
    WEB_DEVELOPMENT("Web Development"),
    MOBILE("Mobile"),
    DEVOPS("DevOps"),
    CYBERSECURITY("Cybersecurity"),
    IT_GENERAL("IT (General)");

    private final String label;

    ProfileArea(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Resolves a profile area from free-form text — either the enum constant name
     * (e.g. {@code SOFTWARE_DEVELOPMENT}) or the human label (e.g.
     * {@code "Software Development"}), case-insensitively and ignoring surrounding
     * whitespace. Also tolerates spaces or hyphens in place of underscores, so a
     * spreadsheet value like {@code "web development"} matches {@code WEB_DEVELOPMENT}.
     *
     * @return the matching {@link ProfileArea}, or {@code null} if the value is
     *         blank or does not match any area
     */
    public static ProfileArea fromLabelOrName(String value) {
        if (value == null) return null;
        String v = value.trim();
        if (v.isEmpty()) return null;
        for (ProfileArea p : values()) {
            if (p.name().equalsIgnoreCase(v) || p.label.equalsIgnoreCase(v)) {
                return p;
            }
        }
        String normalized = v.replaceAll("[\\s-]+", "_").toUpperCase();
        for (ProfileArea p : values()) {
            if (p.name().equals(normalized)) return p;
        }
        return null;
    }
}
