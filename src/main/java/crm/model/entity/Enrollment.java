package crm.model.entity;

import lombok.*;
import crm.model.enums.EnrollmentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true)
public class Enrollment extends BaseEntity {

    private Long sessionId;
    private Long contactId;
    private LocalDateTime enrollmentDate;
    private EnrollmentStatus status;
    private Integer attendedSessions;
    private BigDecimal attendanceRate;
    private Boolean examPassed;
    private BigDecimal finalGrade;
    private Boolean certificateIssued;
    private String certificateNumber;
    private Integer rating;
    private String feedback;
    private String notes;
}
