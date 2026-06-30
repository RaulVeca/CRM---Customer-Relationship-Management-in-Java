package crm.model.entity;

import lombok.*;

/**
 * A trainer that delivers the courses. Backed by the {@code trainers} table,
 * which stores only the name and the email.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true)
public class Trainer extends BaseEntity {

    private String firstName;
    private String lastName;
    private String email;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
