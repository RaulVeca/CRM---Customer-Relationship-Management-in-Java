package crm.web.controller;

import crm.dao.AdminDao;
import crm.dao.ContactDao;
import crm.dao.EmployeeDao;
import crm.web.dto.ApiError;
import crm.web.dto.AuthResponse;
import crm.web.dto.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Email-only authentication for the single sign-in card on the landing page.
 *
 * <p>There is one endpoint, but the roles stay strictly separated by the data:
 * the email is resolved against the {@code admins} table, then the
 * {@code contacts} table, then the {@code employees} table, and the table it
 * belongs to decides the role returned. Admins get an ADMIN session (admin
 * portal); contacts and employees both get a USER session (client portal). A
 * contact or employee can therefore never obtain an admin session, and an admin
 * can never obtain a user one.</p>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final ContactDao contactDao = ContactDao.getInstance();
    private final AdminDao adminDao = AdminDao.getInstance();
    private final EmployeeDao employeeDao = EmployeeDao.getInstance();

    /**
     * Single email login. Admins are checked first, then contacts, then
     * employees; the matching table sets the role (admins → ADMIN portal,
     * contacts and employees → client portal). An unknown email is rejected.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest http) {
        String email = normalize(request);
        if (email.isEmpty()) {
            return badRequest(http);
        }
        return adminDao.findByEmail(email)
                .map(admin -> ResponseEntity.ok((Object) AuthResponse.admin(admin)))
                .or(() -> contactDao.findByEmail(email)
                        .map(contact -> ResponseEntity.ok((Object) AuthResponse.user(contact))))
                .or(() -> employeeDao.findByEmail(email)
                        .map(employee -> ResponseEntity.ok((Object) AuthResponse.employee(employee))))
                .orElseGet(() -> unauthorized(http,
                        "Acest email nu este înregistrat."));
    }

    private String normalize(LoginRequest request) {
        return request == null || request.email() == null ? "" : request.email().trim();
    }

    private ResponseEntity<Object> badRequest(HttpServletRequest http) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiError.of(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "Adresa de email este obligatorie.", http.getRequestURI(), null));
    }

    private ResponseEntity<Object> unauthorized(HttpServletRequest http, String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiError.of(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                        message, http.getRequestURI(), null));
    }
}
