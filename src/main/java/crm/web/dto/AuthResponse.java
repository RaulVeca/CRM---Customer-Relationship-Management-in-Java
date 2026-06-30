package crm.web.dto;

import crm.model.entity.Admin;
import crm.model.entity.Contact;
import crm.model.entity.Employee;

/**
 * Identity returned to the front-end after a successful email sign-in. The
 * {@code role} ("USER" or "ADMIN") tells the client which area to open and is
 * kept in the browser session.
 */
public record AuthResponse(
        String role,
        Long id,
        String firstName,
        String lastName,
        String email
) {
    public static AuthResponse user(Contact c) {
        return new AuthResponse("USER", c.getId(), c.getFirstName(), c.getLastName(), c.getEmail());
    }

    public static AuthResponse admin(Admin a) {
        return new AuthResponse("ADMIN", a.getId(), a.getFirstName(), a.getLastName(), a.getEmail());
    }

    /**
     * An employee signs in as a regular USER, landing on the same client portal
     * as contacts.
     */
    public static AuthResponse employee(Employee e) {
        return new AuthResponse("USER", e.getId(), e.getFirstName(), e.getLastName(), e.getEmail());
    }
}
