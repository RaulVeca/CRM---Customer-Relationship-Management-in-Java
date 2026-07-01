package crm.model.enums;

public enum CourseCategory {
    PROGRAMMING("Programming"),
    AI("Artificial Intelligence"),
    DATA_SCIENCE("Data Science"),
    WEB_DEVELOPMENT("Web Development"),
    MOBILE("Mobile"),
    DEVOPS("DevOps"),
    CYBERSECURITY("Cybersecurity"),
    PROFESSIONAL_RECONVERSION("Career Change"),
    WORKSHOP("Workshop");

    private final String label;

    CourseCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
