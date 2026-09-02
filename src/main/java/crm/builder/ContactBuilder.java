package crm.builder;

import crm.model.entity.Contact;
import crm.model.enums.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * BUILDER PATTERN - ContactBuilder
 * 
 * Permite construcția fluentă a obiectelor Contact cu validări.
 * Util când există multe câmpuri opționale, înlocuind constructori multipli.
 * 
 * Exemplu de utilizare:
 * <pre>
 * Contact c = new ContactBuilder()
 *     .asIndividual()
 *     .name("Ion", "Popescu")
 *     .email("ion@example.com")
 *     .phone("0712345678")
 *     .leadSource(LeadSource.WEBSITE)
 *     .withGdprConsent()
 *     .build();
 * </pre>
 * 
 * Notă: Lombok generează un @Builder de bază, dar ContactBuilder
 * oferă o API mai expresivă și validări custom.
 */
public class ContactBuilder {

    private final Contact contact;

    public ContactBuilder() {
        this.contact = new Contact();
        this.contact.setLeadStatus(LeadStatus.NEW);
        this.contact.setLeadScore(0);
        this.contact.setGdprConsent(false);
        this.contact.setMarketingConsent(false);
        this.contact.setFirstContactDate(LocalDateTime.now());
    }

    public ContactBuilder asIndividual() {
        contact.setContactType(ContactType.INDIVIDUAL);
        return this;
    }

    public ContactBuilder name(String firstName, String lastName) {
        contact.setFirstName(firstName);
        contact.setLastName(lastName);
        return this;
    }

    public ContactBuilder email(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email: " + email);
        }
        contact.setEmail(email.trim().toLowerCase());
        return this;
    }

    public ContactBuilder phone(String phone) {
        contact.setPhone(phone);
        return this;
    }

    public ContactBuilder birthDate(LocalDate birthDate) {
        if (birthDate != null && birthDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("The date of birth cannot be in the future");
        }
        contact.setBirthDate(birthDate);
        return this;
    }

    public ContactBuilder address(String street, String city, String county, String postalCode) {
        contact.setAddressStreet(street);
        contact.setAddressCity(city);
        contact.setAddressCounty(county);
        contact.setAddressPostalCode(postalCode);
        return this;
    }

    public ContactBuilder leadSource(LeadSource source) {
        contact.setLeadSource(source);
        return this;
    }

    public ContactBuilder experienceLevel(ExperienceLevel level) {
        contact.setExperienceLevel(level);
        return this;
    }

    public ContactBuilder learningGoal(String goal) {
        contact.setLearningGoal(goal);
        return this;
    }

    public ContactBuilder withGdprConsent() {
        contact.setGdprConsent(true);
        contact.setGdprConsentDate(LocalDateTime.now());
        return this;
    }

    public Contact build() {
        validate();
        return contact;
    }

    private void validate() {
        if (contact.getContactType() == null) {
            throw new IllegalStateException("ContactType is required");
        }
        if (contact.getEmail() == null || contact.getEmail().isEmpty()) {
            throw new IllegalStateException("The email is required");
        }
        if (contact.getContactType() == ContactType.INDIVIDUAL
                && contact.getFirstName() == null && contact.getLastName() == null) {
            throw new IllegalStateException(
                "First or last name is required for an individual");
        }
    }
}
