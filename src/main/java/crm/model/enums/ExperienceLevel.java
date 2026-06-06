package crm.model.enums;

public enum ExperienceLevel {
    BEGINNER("Începător"),
    INTERMEDIATE("Intermediar"),
    ADVANCED("Avansat");

    private final String label;

    ExperienceLevel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
