package crm.model.entity;

import lombok.*;

import java.math.BigDecimal;

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
    private String phone;
    private String bio;
    private String specializations;
    private Integer yearsExperience;
    private BigDecimal hourlyRate;
    private BigDecimal averageRating;
    private Integer totalSessions;
    private Boolean active;
    private Long userId;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
