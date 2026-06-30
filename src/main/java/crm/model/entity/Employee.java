package crm.model.entity;

import crm.model.enums.ProfileArea;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * An individual employee of a corporate client ({@link Contact} of type
 * CORPORATE).
 *
 * <p>Beyond a plain headcount, each employee carries a <b>work profile</b> (the
 * area they currently work in) and a set of <b>interest profiles</b> (areas they
 * want to grow into, e.g. Machine Learning, AI). This richer model is what the
 * AI recommendation engine uses to suggest relevant courses.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true)
public class Employee extends BaseEntity {

    /** The corporate client this employee belongs to (contacts.id). */
    private Long companyId;

    private String firstName;
    private String lastName;
    private String email;
    private String jobTitle;

    /** Primary area the employee currently works in. */
    private ProfileArea workProfile;

    /** Areas the employee is interested in developing. */
    @Builder.Default
    private List<ProfileArea> interestProfiles = new ArrayList<>();

    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        if (firstName != null) sb.append(firstName);
        if (lastName != null) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(lastName);
        }
        return sb.toString();
    }
}
