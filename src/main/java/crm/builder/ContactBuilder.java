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

    public ContactBuilder asCorporate() {
        contact.setContactType(ContactType.CORPORATE);
        return this;
    }

    public ContactBuilder name(String firstName, String lastName) {
        contact.setFirstName(firstName);
        contact.setLastName(lastName);
        return this;
    }

    public ContactBuilder companyName(String companyName) {
        contact.setCompanyName(companyName);
        contact.setContactType(ContactType.CORPORATE);
        return this;
    }

    public ContactBuilder email(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Email invalid: " + email);
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
            throw new IllegalArgumentException("Data nașterii nu poate fi în viitor");
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

    public ContactBuilder fiscalDetails(String fiscalCode, String registrationNumber) {
        contact.setFiscalCode(fiscalCode);
        contact.setRegistrationNumber(registrationNumber);
        return this;
    }

    public ContactBuilder industry(String industry, Integer employeeCount) {
        contact.setIndustry(industry);
        contact.setEmployeeCount(employeeCount);
        return this;
    }

    public ContactBuilder leadSource(LeadSource source) {
        contact.setLeadSource(source);
        return this;
    }

    public ContactBuilder leadStatus(LeadStatus status) {
        contact.setLeadStatus(status);
        return this;
    }

    public ContactBuilder leadScore(int score) {
        contact.setLeadScore(Math.max(0, Math.min(100, score)));
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

    public ContactBuilder assignedTo(Long userId) {
        contact.setAssignedTo(userId);
        return this;
    }

    public ContactBuilder withGdprConsent() {
        contact.setGdprConsent(true);
        contact.setGdprConsentDate(LocalDateTime.now());
        return this;
    }

    public ContactBuilder withMarketingConsent() {
        contact.setMarketingConsent(true);
        return this;
    }

    public Contact build() {
        validate();
        return contact;
    }

    private void validate() {
        if (contact.getContactType() == null) {
            throw new IllegalStateException("ContactType este obligatoriu");
        }
        if (contact.getEmail() == null || contact.getEmail().isEmpty()) {
            throw new IllegalStateException("Email-ul este obligatoriu");
        }
        if (contact.getContactType() == ContactType.INDIVIDUAL) {
            if (contact.getFirstName() == null && contact.getLastName() == null) {
                throw new IllegalStateException(
                    "Prenume sau nume obligatoriu pentru persoană fizică");
            }
        } else if (contact.getContactType() == ContactType.CORPORATE) {
            if (contact.getCompanyName() == null || contact.getCompanyName().isEmpty()) {
                throw new IllegalStateException("Numele companiei este obligatoriu");
            }
        }
    }
}
