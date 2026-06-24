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
}
