package crm.web.dto;

/**
 * Sign-in payload: an email plus the account's password. Shared by every role —
 * the email resolves the account (admin, contact or employee) and the password
 * is then verified against that account's stored password.
 */
public record LoginRequest(String email, String password) {
}
