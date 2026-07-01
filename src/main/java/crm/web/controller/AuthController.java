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
            return badRequest(http, "Adresa de email este obligatorie.");
        }
        if (password.isEmpty()) {
            return badRequest(http, "Parola este obligatorie.");
        }

        var admin = adminDao.findByEmail(email);
        if (admin.isPresent()) {
            if (!passwordMatches(adminDao.findPasswordByEmail(email), password)) {
                return unauthorized(http, "Parolă incorectă.");
            }
            return ResponseEntity.ok(AuthResponse.admin(admin.get()));
        }
        var contact = contactDao.findByEmail(email);
        if (contact.isPresent()) {
            if (!passwordMatches(contactDao.findPasswordByEmail(email), password)) {
                return unauthorized(http, "Parolă incorectă.");
            }
            return ResponseEntity.ok(AuthResponse.user(contact.get()));
        }
        var employee = employeeDao.findByEmail(email);
        if (employee.isPresent()) {
            if (!passwordMatches(employeeDao.findPasswordByEmail(email), password)) {
                return unauthorized(http, "Parolă incorectă.");
            }
            return ResponseEntity.ok(AuthResponse.employee(employee.get()));
        }
        return unauthorized(http, "Acest email nu este înregistrat.");
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
            return badRequest(http, "Datele de înregistrare sunt obligatorii.");
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

        // Toate cardurile sunt obligatorii pentru înregistrare.
        List<String> errors = new ArrayList<>();
        requireField(errors, firstName, "Prenumele");
        requireField(errors, lastName, "Numele");
        if (birthDate == null) {
            errors.add("Data nașterii este obligatorie.");
        } else if (birthDate.isAfter(LocalDate.now())) {
            errors.add("Data nașterii nu poate fi în viitor.");
        }
        requireField(errors, email, "Adresa de email");
        requireField(errors, phone, "Numărul de telefon");
        requireField(errors, street, "Strada");
        requireField(errors, city, "Orașul");
        requireField(errors, county, "Județul");
        requireField(errors, postalCode, "Codul poștal");
        requireField(errors, learningGoal, "Obiectivul de învățare");
        if (password.isEmpty()) {
            errors.add("Parola este obligatorie.");
        } else if (password.length() < 4) {
            errors.add("Parola trebuie să aibă cel puțin 4 caractere.");
        } else if (!password.equals(confirmPassword)) {
            errors.add("Parolele nu coincid.");
        }
        if (experienceLevel == null) {
            errors.add("Nivelul de experiență este obligatoriu.");
        }
        if (leadSource == null) {
            errors.add("Sursa (cum ai aflat de noi) este obligatorie.");
        }
        if (!Boolean.TRUE.equals(request.gdprConsent())) {
            errors.add("Consimțământul GDPR este obligatoriu pentru înregistrare.");
        }
        if (!errors.isEmpty()) {
            return validationError(http, errors);
        }

        // Un email deja salvat în baza de date (admin, angajat sau contact) nu
        // se mai poate înregistra — se poate doar autentifica. Doar tabela
        // contacts poate crește, și doar cu emailuri complet noi.
        if (adminDao.findByEmail(email).isPresent()
                || employeeDao.findByEmail(email).isPresent()
                || contactDao.findByEmail(email).isPresent()) {
            return conflict(http,
                    "Acest email este deja înregistrat. Autentifică-te în loc să creezi un cont nou.");
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
            errors.add(label + " este obligatoriu.");
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
                        "Completează toate câmpurile obligatorii.", http.getRequestURI(), errors));
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
