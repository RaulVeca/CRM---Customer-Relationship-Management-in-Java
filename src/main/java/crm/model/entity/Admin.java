package crm.model.entity;

import lombok.*;

/**
 * Minimal account for the "Cont admin" sign-in option. Backed by the
 * {@code admins} table, which holds the trainers that may access the admin
 * back-office. Only the name and the email are stored - authentication is by
 * email alone.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true)
public class Admin extends BaseEntity {

    private String firstName;
    private String lastName;
    private String email;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
