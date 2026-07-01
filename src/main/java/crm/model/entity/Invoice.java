package crm.model.entity;

import lombok.*;
import crm.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Factură generată automat pentru o rezervare (booking) de ședință online cu un
 * trainer - vezi {@link MeditationSession}.
 *
 * <p>Suma nu este fixă: se calculează ca {@code hours × hourlyRate}. Conturile de
 * angajat primesc un discount automat ({@code discountRate}, ex. 0.60 = −60%), deci
 * {@code total = subtotal − discountAmount}. Prețurile sunt în USD, fără TVA -
 * exact suma pe care clientul o transferă la programare.</p>
 *
 * <p>Rezervarea se face doar după ce clientul a transferat banii, deci factura este
 * de la bun început {@code PAID} (o chitanță) - nu există termen de plată.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true)
public class Invoice extends BaseEntity {

    private String invoiceNumber;
    private Long sessionId;        // rezervarea (MeditationSession) facturată
    private Long clientId;         // contul care a făcut rezervarea
    private String clientEmail;
    private LocalDate issueDate;
    private int hours;             // durata ședinței, în ore
    private BigDecimal hourlyRate; // tariful pe oră (USD)
    private BigDecimal subtotal;   // gross = hours × hourlyRate
    private BigDecimal discountRate;   // 0.00 sau 0.60 (angajat)
    private BigDecimal discountAmount; // subtotal × discountRate
    private BigDecimal total;      // net = subtotal − discountAmount
    private BigDecimal paidAmount;
    private PaymentStatus status;
    private LocalDate paymentDate;
    private String description;

    public BigDecimal getRemainingAmount() {
        BigDecimal paid = paidAmount != null ? paidAmount : BigDecimal.ZERO;
        return total.subtract(paid);
    }
}
