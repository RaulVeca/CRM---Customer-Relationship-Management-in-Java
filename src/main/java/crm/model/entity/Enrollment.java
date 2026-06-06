package crm.model.entity;

import lombok.*;
import crm.model.enums.EnrollmentStatus;
import crm.model.enums.PaymentStatus;

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
    private BigDecimal price;
    private BigDecimal discount;
    private BigDecimal finalPrice;
    private PaymentStatus paymentStatus;
    private BigDecimal paidAmount;
    private Integer attendedSessions;
    private BigDecimal attendanceRate;
    private Boolean examPassed;
    private BigDecimal finalGrade;
    private Boolean certificateIssued;
    private String certificateNumber;
    private Integer rating;
    private String feedback;
    private String notes;

    public BigDecimal getRemainingAmount() {
        BigDecimal final_ = finalPrice != null ? finalPrice : price;
        BigDecimal paid = paidAmount != null ? paidAmount : BigDecimal.ZERO;
        return final_.subtract(paid);
    }
}
