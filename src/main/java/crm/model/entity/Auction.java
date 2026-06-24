package crm.model.entity;

import crm.model.enums.AuctionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * An auction in which corporate clients bid to win the right to run a course.
 *
 * <p>Per the business requirement, only companies that have an opportunity in
 * the NEGOTIATION stage may bid; the highest bidder wins the course.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true)
public class Auction extends BaseEntity {

    private Long courseId;
    private String title;
    private String description;
    private BigDecimal startingPrice;
    private AuctionStatus status;
    private LocalDateTime closesAt;

    /** Set once the auction is awarded. */
    private Long winnerCompanyId;
    private BigDecimal winningAmount;

    public boolean isOpen() {
        return status == AuctionStatus.OPEN;
    }
}
