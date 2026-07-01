package crm.web.dto;

import java.time.LocalDate;

/**
 * Self-service registration payload. Mirrors the "cards" of an INDIVIDUAL
 * contact: every field the visitor fills on the register form maps to a column
 * of the {@code contacts} table. Registration only ever creates a new
 * {@code contacts} row — no other table is touched — and only for the
 * {@code INDIVIDUAL} contact type.
 */
public record RegisterRequest(
        String firstName,
        String lastName,
        LocalDate birthDate,
        String email,
        String phone,
        String addressStreet,
        String addressCity,
        String addressCounty,
        String addressPostalCode,
        String experienceLevel,
        String learningGoal,
        String leadSource,
        String password,
        String confirmPassword,
        Boolean gdprConsent,
        Boolean marketingConsent
) {
}
