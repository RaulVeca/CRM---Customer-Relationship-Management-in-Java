package crm.web.controller;

import crm.builder.ContactBuilder;
import crm.dao.AdminDao;
import crm.dao.ContactDao;
import crm.dao.EmployeeDao;
import crm.facade.CrmFacade;
import crm.model.entity.Contact;
import crm.model.enums.ExperienceLevel;
import crm.model.enums.LeadSource;
import crm.web.dto.ApiError;
import crm.web.dto.AuthResponse;
import crm.web.dto.LoginRequest;
import crm.web.dto.RegisterRequest;
import crm.web.dto.ResetPasswordRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Email + password authentication plus self-service registration for the landing
 * page.
 *
 * <p>There is one login endpoint, but the roles stay strictly separated by the
 * data: the email is resolved against the {@code admins} table, then the
 * {@code contacts} table, then the {@code employees} table, and the table it
 * belongs to decides the role returned. Admins get an ADMIN session (admin
 * portal); contacts and employees both get a USER session (client portal). A
 * contact or employee can therefore never obtain an admin session, and an admin
 * can never obtain a user one.</p>
 *
 * <p>Registration ({@link #register}) is the only way a visitor can create an
 * account, and it exclusively adds a row to the {@code contacts} table for an
 * {@code INDIVIDUAL} contact — no admin or employee is ever created this way.</p>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final ContactDao contactDao = ContactDao.getInstance();
    private final AdminDao adminDao = AdminDao.getInstance();
    private final EmployeeDao employeeDao = EmployeeDao.getInstance();

    private final CrmFacade facade;

    public AuthController(CrmFacade facade) {
        this.facade = facade;
    }

    /**
     * Email + password login. The email is resolved against the admins table
     * first, then contacts, then employees; the matching table sets the role
     * (admins → ADMIN portal, contacts and employees → client portal). Once the
     * account is found, the supplied password is verified against the password
     * stored for it. An unknown email or a wrong password is rejected.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest http) {
        String email = normalize(request);
        String password = request == null || request.password() == null ? "" : request.password();
        if (email.isEmpty()) {
            return badRequest(http, "The email address is required.");
        }
        if (password.isEmpty()) {
            return badRequest(http, "The password is required.");
        }

        var admin = adminDao.findByEmail(email);
        if (admin.isPresent()) {
            if (!passwordMatches(adminDao.findPasswordByEmail(email), password)) {
                return unauthorized(http, "Incorrect password.");
            }
            return ResponseEntity.ok(AuthResponse.admin(admin.get()));
        }
        var contact = contactDao.findByEmail(email);
        if (contact.isPresent()) {
            if (!passwordMatches(contactDao.findPasswordByEmail(email), password)) {
                return unauthorized(http, "Incorrect password.");
            }
            return ResponseEntity.ok(AuthResponse.user(contact.get()));
        }
        var employee = employeeDao.findByEmail(email);
        if (employee.isPresent()) {
            if (!passwordMatches(employeeDao.findPasswordByEmail(email), password)) {
                return unauthorized(http, "Incorrect password.");
            }
            return ResponseEntity.ok(AuthResponse.employee(employee.get()));
        }
        return unauthorized(http, "This email is not registered.");
    }

    /**
     * Self-service registration for an INDIVIDUAL contact. Every "card" of the
     * form is required; when all are valid a single new row is inserted into the
     * {@code contacts} table (via the existing facade, so scoring and observers
     * run as usual) and the caller is returned a USER session, exactly as if it
     * had just logged in.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request, HttpServletRequest http) {
        if (request == null) {
            return badRequest(http, "Registration details are required.");
        }

        String firstName = trim(request.firstName());
        String lastName = trim(request.lastName());
        String email = trim(request.email()).toLowerCase();
        String phone = trim(request.phone());
        String street = trim(request.addressStreet());
        String city = trim(request.addressCity());
        String county = trim(request.addressCounty());
        String postalCode = trim(request.addressPostalCode());
        String learningGoal = trim(request.learningGoal());
        // Passwords are not trimmed — leading/trailing characters are significant.
        String password = request.password() == null ? "" : request.password();
        String confirmPassword = request.confirmPassword() == null ? "" : request.confirmPassword();
        LocalDate birthDate = request.birthDate();
        ExperienceLevel experienceLevel = parseEnum(request.experienceLevel(), ExperienceLevel.class);
        LeadSource leadSource = parseEnum(request.leadSource(), LeadSource.class);

        // All cards are required for registration.
        List<String> errors = new ArrayList<>();
        requireField(errors, firstName, "First name");
        requireField(errors, lastName, "Last name");
        if (birthDate == null) {
            errors.add("The date of birth is required.");
        } else if (birthDate.isAfter(LocalDate.now())) {
            errors.add("The date of birth cannot be in the future.");
        }
        requireField(errors, email, "Email address");
        requireField(errors, phone, "Phone number");
        requireField(errors, street, "Street");
        requireField(errors, city, "City");
        requireField(errors, county, "County");
        requireField(errors, postalCode, "Postal code");
        requireField(errors, learningGoal, "Learning goal");
        if (password.isEmpty()) {
            errors.add("The password is required.");
        } else if (password.length() < 4) {
            errors.add("The password must be at least 4 characters.");
        } else if (!password.equals(confirmPassword)) {
            errors.add("The passwords don't match.");
        }
        if (experienceLevel == null) {
            errors.add("The experience level is required.");
        }
        if (leadSource == null) {
            errors.add("The source (how you heard about us) is required.");
        }
        if (!Boolean.TRUE.equals(request.gdprConsent())) {
            errors.add("GDPR consent is required for registration.");
        }
        if (!errors.isEmpty()) {
            return validationError(http, errors);
        }

        // An email already stored in the database (admin, employee or contact) can
        // no longer register — it can only sign in. Only the contacts table may
        // grow, and only with brand-new emails.
        if (adminDao.findByEmail(email).isPresent()
                || employeeDao.findByEmail(email).isPresent()
                || contactDao.findByEmail(email).isPresent()) {
            return conflict(http,
                    "This email is already registered. Log in instead of creating a new account.");
        }

        Contact contact = new ContactBuilder()
                .asIndividual()
                .name(firstName, lastName)
                .birthDate(birthDate)
                .email(email)
                .phone(phone)
                .address(street, city, county, postalCode)
                .leadSource(leadSource)
                .experienceLevel(experienceLevel)
                .learningGoal(learningGoal)
                .withGdprConsent()
                .build();
        if (Boolean.TRUE.equals(request.marketingConsent())) {
            contact.setMarketingConsent(true);
        }

        Contact saved = facade.createContact(contact);
        // Store the chosen password on the freshly created contact row (its column
        // otherwise defaults to the shared '1234').
        contactDao.updatePassword(saved.getId(), password);
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.user(saved));
    }

    /**
     * Password recovery for a client who forgot their password. Because there is
     * no real email delivery, the client does not receive a reset link — instead
     * they prove ownership of the account with two facts already on their
     * {@code contacts} row: the birth date and the phone number. The email must
     * belong to the {@code contacts} table (admins and employees cannot reset a
     * password here), and both the birth date and the phone must match what is
     * stored. Only then is the chosen new password saved. To avoid leaking which
     * accounts exist, an unknown email and a wrong birth date/phone return the
     * same generic message.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request, HttpServletRequest http) {
        if (request == null) {
            return badRequest(http, "Reset details are required.");
        }

        String email = trim(request.email()).toLowerCase();
        String phone = trim(request.phone());
        LocalDate birthDate = request.birthDate();
        // Passwords are not trimmed — leading/trailing characters are significant.
        String newPassword = request.newPassword() == null ? "" : request.newPassword();
        String confirmPassword = request.confirmPassword() == null ? "" : request.confirmPassword();

        List<String> errors = new ArrayList<>();
        requireField(errors, email, "Email address");
        if (birthDate == null) {
            errors.add("The date of birth is required.");
        }
        requireField(errors, phone, "Phone number");
        if (newPassword.isEmpty()) {
            errors.add("The new password is required.");
        } else if (newPassword.length() < 4) {
            errors.add("The password must be at least 4 characters.");
        } else if (!newPassword.equals(confirmPassword)) {
            errors.add("The passwords don't match.");
        }
        if (!errors.isEmpty()) {
            return validationError(http, errors);
        }

        // Recovery is only for clients: resolve the email against contacts alone.
        // An admin or employee email simply won't be found here.
        var contact = contactDao.findByEmail(email);
        boolean identityMatches = contact.isPresent()
                && birthDate.equals(contact.get().getBirthDate())
                && digitsOnly(phone).equals(digitsOnly(contact.get().getPhone()));
        if (!identityMatches) {
            return unauthorized(http,
                    "The details entered do not match a client account. "
                            + "Check the email, date of birth and phone number.");
        }

        contactDao.updatePassword(contact.get().getId(), newPassword);
        return ResponseEntity.ok(Map.of(
                "message", "The password has been changed. You can now log in with the new password."));
    }

    /**
     * Keeps only the digits of a phone number so two numbers compare equal
     * regardless of spaces, dashes or a leading {@code +} / {@code 0040} prefix
     * difference in how they were typed.
     */
    private String digitsOnly(String phone) {
        return phone == null ? "" : phone.replaceAll("\\D", "");
    }

    private String normalize(LoginRequest request) {
        return request == null || request.email() == null ? "" : request.email().trim();
    }

    private boolean passwordMatches(String stored, String provided) {
        return stored != null && stored.equals(provided);
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private void requireField(List<String> errors, String value, String label) {
        if (value == null || value.isEmpty()) {
            errors.add(label + " is required.");
        }
    }

    private <E extends Enum<E>> E parseEnum(String value, Class<E> enumClass) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, value.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private ResponseEntity<Object> badRequest(HttpServletRequest http, String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiError.of(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        message, http.getRequestURI(), null));
    }

    private ResponseEntity<Object> validationError(HttpServletRequest http, List<String> errors) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiError.of(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "Fill in all required fields.", http.getRequestURI(), errors));
    }

    private ResponseEntity<Object> conflict(HttpServletRequest http, String message) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ApiError.of(HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.getReasonPhrase(),
                        message, http.getRequestURI(), null));
    }

    private ResponseEntity<Object> unauthorized(HttpServletRequest http, String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiError.of(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                        message, http.getRequestURI(), null));
    }
}
