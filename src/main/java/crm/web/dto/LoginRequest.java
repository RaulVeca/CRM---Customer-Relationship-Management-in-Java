package crm.web.dto;

/**
 * Email-only sign-in payload, shared by the user and the admin login endpoints.
 */
public record LoginRequest(String email) {
}
