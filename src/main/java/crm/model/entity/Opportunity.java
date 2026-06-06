package crm.model.entity;

import lombok.*;
import crm.model.enums.DeliveryMode;
import crm.model.enums.OpportunityStage;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true)
public class Opportunity extends BaseEntity {

    private Long clientId;
    private String title;
    private String description;
    private Integer estimatedParticipants;
    private String customRequirements;
    private DeliveryMode deliveryMode;
    private String preferredLocation;
    private LocalDate desiredStartDate;
    private BigDecimal estimatedValue;
    private BigDecimal quotedValue;
    private Integer probabilityPercent;
    private OpportunityStage stage;
    private LocalDate expectedCloseDate;
    private LocalDate actualCloseDate;
    private Long assignedTo;
    private String competitors;
    private String lostReason;

    public boolean isClosed() {
        return stage == OpportunityStage.WON || stage == OpportunityStage.LOST;
    }

    public boolean isWon() {
        return stage == OpportunityStage.WON;
    }

    public BigDecimal getWeightedValue() {
        if (quotedValue == null || probabilityPercent == null) {
            return BigDecimal.ZERO;
        }
        return quotedValue.multiply(BigDecimal.valueOf(probabilityPercent))
                .divide(BigDecimal.valueOf(100));
    }
}
