package crm.model.entity;

import lombok.*;
import crm.model.enums.CourseCategory;
import crm.model.enums.ExperienceLevel;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true)
public class Course extends BaseEntity {

    private String code;
    private String name;
    private String description;
    private String syllabus;
    private CourseCategory category;
    private ExperienceLevel level;
    private String prerequisites;
    private Integer durationHours;
    private BigDecimal priceIndividual;
    private BigDecimal priceGroup;
    private BigDecimal priceCorporatePerDay;
    private Integer minParticipants;
    private Integer maxParticipants;
    private Boolean active;
}
