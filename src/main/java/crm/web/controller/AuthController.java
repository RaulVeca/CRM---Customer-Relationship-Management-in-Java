package crm.web.controller;

import crm.dao.AdminDao;
import crm.dao.ContactDao;
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
 * Email-only authentication for the two sign-in options on the landing page.
 *
 * <p>The two roles are strictly separated: {@code /login/user} only ever checks
 * the {@code contacts} table and {@code /login/admin} only ever checks the
 * {@code admins} table. A contact therefore cannot sign in as an admin and an
 * admin cannot sign in as a user.</p>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final ContactDao contactDao = ContactDao.getInstance();
    private final AdminDao adminDao = AdminDao.getInstance();

    /** "Cont utilizator" - authenticates against the contacts table only. */
    @PostMapping("/login/user")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request, HttpServletRequest http) {
        String email = normalize(request);
        if (email.isEmpty()) {
            return badRequest(http);
        }
        return contactDao.findByEmail(email)
                .map(contact -> ResponseEntity.ok((Object) AuthResponse.user(contact)))
                .orElseGet(() -> unauthorized(http,
                        "Acest email nu este înregistrat ca utilizator."));
    }

    /** "Cont admin" - authenticates against the admins table only. */
    @PostMapping("/login/admin")
    public ResponseEntity<?> loginAdmin(@RequestBody LoginRequest request, HttpServletRequest http) {
        String email = normalize(request);
        if (email.isEmpty()) {
            return badRequest(http);
        }
        return adminDao.findByEmail(email)
                .map(admin -> ResponseEntity.ok((Object) AuthResponse.admin(admin)))
                .orElseGet(() -> unauthorized(http,
                        "Acest email nu este înregistrat ca administrator."));
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
