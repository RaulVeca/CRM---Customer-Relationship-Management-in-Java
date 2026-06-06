package crm.model.entity;

import lombok.*;
import crm.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true)
public class Invoice extends BaseEntity {

    private String invoiceNumber;
    private Long contractId;
    private Long enrollmentId;
    private Long clientId;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal total;
    private BigDecimal paidAmount;
    private PaymentStatus status;
    private LocalDate paymentDate;
    private String description;

    public boolean isOverdue() {
        return status != PaymentStatus.PAID 
            && dueDate != null 
            && dueDate.isBefore(LocalDate.now());
    }

    public BigDecimal getRemainingAmount() {
        BigDecimal paid = paidAmount != null ? paidAmount : BigDecimal.ZERO;
        return total.subtract(paid);
    }
}
