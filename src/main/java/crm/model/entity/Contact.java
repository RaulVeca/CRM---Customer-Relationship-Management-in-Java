package crm.model.entity;

import lombok.*;
import crm.model.enums.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Entitate Contact - reprezintă atât lead-uri B2C cât și clienți B2B.
 * Folosește polimorfism prin câmpul contactType.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true)

public class Contact extends BaseEntity {

    private ContactType contactType;

    // Date individuale (B2C)
    private String firstName;
    private String lastName;
    private LocalDate birthDate;

    // Date corporate (B2B)
    private String companyName;
    private String fiscalCode;
    private String registrationNumber;
    private String industry;
    private Integer employeeCount;//

    // Date comune
    private String email;
    private String phone;
    private String addressStreet;
    private String addressCity;
    private String addressCounty;
    private String addressPostalCode;

    // Lead management
    private LeadSource leadSource;
    private LeadStatus leadStatus;
    private Integer leadScore;
    private ExperienceLevel experienceLevel;
    private String learningGoal;

    // Tracking
    private LocalDateTime firstContactDate;
    private LocalDateTime lastContactDate;
    private Long assignedTo;

    // GDPR
    private Boolean gdprConsent;
    private LocalDateTime gdprConsentDate;
    private Boolean marketingConsent;

    /**
     * Returnează numele complet folosind Optional (Java 8).
     */
    public Optional<String> getFullName() {
        if (contactType == ContactType.CORPORATE) {
            return Optional.ofNullable(companyName);
        }
        StringBuilder sb = new StringBuilder();
        if (firstName != null) sb.append(firstName);
        if (lastName != null) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(lastName);
        }
        return sb.length() > 0 ? Optional.of(sb.toString()) : Optional.empty();
    }

    public boolean isCorporate() {
        return contactType == ContactType.CORPORATE;
    }

    public void setGdprConsentDate(LocalDateTime time) {
        this.gdprConsentDate = time;
    }
}
