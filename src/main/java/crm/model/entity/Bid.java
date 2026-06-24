package crm.model.entity;

import lombok.*;

import java.math.BigDecimal;

/**
 * A single bid placed by a corporate client in a course {@link Auction}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true)
public class Bid extends BaseEntity {

    private Long auctionId;
    private Long companyId;
    /** Denormalized company name for convenient display. */
    private String companyName;
    private BigDecimal amount;
}
