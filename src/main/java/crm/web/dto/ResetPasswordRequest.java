package crm.web.dto;

import java.time.LocalDate;

/**
 * Password-recovery payload for a client. Since there is no real email delivery,
 * a client proves ownership of the account with two facts already stored on the
 * {@code contacts} row — the birth date and the phone number — instead of a
 * mailed reset link. When both match, the chosen new password replaces the old
 * one. Only the {@code contacts} table is ever affected: admins and employees
 * cannot reset a password this way.
 */
public record ResetPasswordRequest(
        String email,
        LocalDate birthDate,
        String phone,
        String newPassword,
        String confirmPassword
) {
}
