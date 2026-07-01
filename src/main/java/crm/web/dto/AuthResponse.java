package crm.web.dto;

import crm.model.entity.Admin;
import crm.model.entity.Contact;
import crm.model.entity.Employee;

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
        return new AuthResponse("USER", c.getId(), c.getFirstName(), c.getLastName(), c.getEmail(), 0.0);
    }

    public static AuthResponse admin(Admin a) {
        return new AuthResponse("ADMIN", a.getId(), a.getFirstName(), a.getLastName(), a.getEmail(), 0.0);
    }

    /**
     * An employee signs in as a regular USER, landing on the same client portal
     * as contacts, but with the automatic {@link #EMPLOYEE_DISCOUNT_RATE}
     * corporate discount applied to session pricing.
     */
    public static AuthResponse employee(Employee e) {
        return new AuthResponse("USER", e.getId(), e.getFirstName(), e.getLastName(), e.getEmail(),
                EMPLOYEE_DISCOUNT_RATE);
    }
}
