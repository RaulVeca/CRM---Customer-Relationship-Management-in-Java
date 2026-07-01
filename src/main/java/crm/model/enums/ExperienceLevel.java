package crm.model.enums;

public enum ExperienceLevel {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced");

    private final String label;

    ExperienceLevel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
