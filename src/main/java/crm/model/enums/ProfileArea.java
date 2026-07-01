package crm.model.enums;

/**
 * Professional profile areas used to describe what an employee works in
 * (their work profile) and what they want to learn (their interest profiles).
 *
 * <p>Each area maps to a {@link CourseCategory} so the AI course-recommendation
 * engine can match an employee's profile against the course catalog.</p>
 */
public enum ProfileArea {
    MACHINE_LEARNING("Machine Learning", CourseCategory.AI),
    ARTIFICIAL_INTELLIGENCE("Artificial Intelligence", CourseCategory.AI),
    DATA_SCIENCE("Data Science", CourseCategory.DATA_SCIENCE),
    SOFTWARE_DEVELOPMENT("Software Development", CourseCategory.PROGRAMMING),
    WEB_DEVELOPMENT("Web Development", CourseCategory.WEB_DEVELOPMENT),
    MOBILE("Mobile", CourseCategory.MOBILE),
    DEVOPS("DevOps", CourseCategory.DEVOPS),
    CYBERSECURITY("Cybersecurity", CourseCategory.CYBERSECURITY),
    IT_GENERAL("IT (General)", CourseCategory.PROGRAMMING);

    private final String label;
    private final CourseCategory relatedCategory;

    ProfileArea(String label, CourseCategory relatedCategory) {
        this.label = label;
        this.relatedCategory = relatedCategory;
    }

    public String getLabel() {
        return label;
    }

    public CourseCategory getRelatedCategory() {
        return relatedCategory;
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
