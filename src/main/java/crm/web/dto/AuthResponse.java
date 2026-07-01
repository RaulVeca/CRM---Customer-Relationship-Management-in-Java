package crm.web.dto;

import crm.model.entity.Admin;
import crm.model.entity.Contact;

/**
 * Identity returned to the front-end after a successful email sign-in. The
 * {@code role} ("USER" or "ADMIN") tells the client which area to open and is
 * kept in the browser session.
 *
 * <p>{@code discountRate} carries the automatic price reduction the account is
 * entitled to (0.0 = none). It is decided here, by the table the account lives
 * in: only accounts stored in the {@code employees} table get the corporate
 * discount, so contacts and admins always receive {@code 0.0}. The client uses
 * it to show the reduced session price.</p>
 */
public record AuthResponse(
        String role,
        Long id,
        String firstName,
        String lastName,
        String email,
        double discountRate
) {
    /** The automatic discount every employee account receives (60%). */
    public static final double EMPLOYEE_DISCOUNT_RATE = 0.60;

    public static AuthResponse user(Contact c) {
        return user(c, 0.0);
    }

    /**
     * A USER (client-portal) session for a contact, carrying the automatic price
     * reduction the account is entitled to. Employees receive
     * {@link #EMPLOYEE_DISCOUNT_RATE}, everyone else {@code 0.0}. An employee
     * always signs in through their {@code contacts} identity (created on first
     * sign-in), so the session {@code id} is a valid contact id usable for
     * booking, "my sessions" and cancel. The discount is decided by whether the
     * email exists in the {@code employees} table — the same rule
     * {@code InvoiceService} uses when billing a booked session — so the price the
     * client is shown always matches the price they are charged.
     */
    public static AuthResponse user(Contact c, double discountRate) {
        return new AuthResponse("USER", c.getId(), c.getFirstName(), c.getLastName(), c.getEmail(), discountRate);
    }

    public static AuthResponse admin(Admin a) {
        return new AuthResponse("ADMIN", a.getId(), a.getFirstName(), a.getLastName(), a.getEmail(), 0.0);
    }
}
