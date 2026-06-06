package crm.model.enums;

public enum CourseCategory {
    PROGRAMMING("Programare"),
    AI("Inteligență Artificială"),
    DATA_SCIENCE("Data Science"),
    WEB_DEVELOPMENT("Dezvoltare Web"),
    MOBILE("Mobile"),
    DEVOPS("DevOps"),
    CYBERSECURITY("Securitate Cibernetică"),
    PROFESSIONAL_RECONVERSION("Reconversie Profesională"),
    WORKSHOP("Workshop");

    private final String label;

    CourseCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
