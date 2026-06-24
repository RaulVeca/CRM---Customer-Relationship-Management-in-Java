package crm.service.auction;

import crm.exception.BusinessException;
import crm.model.entity.Auction;
import crm.model.entity.Bid;
import crm.model.entity.Contact;
import crm.model.entity.Opportunity;
import crm.model.enums.AuctionStatus;
import crm.model.enums.OpportunityStage;
import crm.repository.AuctionRepository;
import crm.repository.BidRepository;
import crm.repository.ContactRepository;
import crm.repository.OpportunityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

/**
 * SINGLETON - business logic for course auctions (bidding).
 *
 * <p>Implements the requirement: companies in the NEGOTIATION stage bid for a
 * course, and the highest bidder wins. The eligibility rule (must have an
 * opportunity in NEGOTIATION) is enforced when a bid is placed.</p>
 */
public class AuctionService {

    private static final Logger logger = LoggerFactory.getLogger(AuctionService.class);
    private static volatile AuctionService instance;

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final ContactRepository contactRepository;
    private final OpportunityRepository opportunityRepository;

    private AuctionService() {
        this.auctionRepository = AuctionRepository.getInstance();
        this.bidRepository = BidRepository.getInstance();
        this.contactRepository = ContactRepository.getInstance();
        this.opportunityRepository = OpportunityRepository.getInstance();
    }

    public static AuctionService getInstance() {
        if (instance == null) {
            synchronized (AuctionService.class) {
                if (instance == null) {
                    instance = new AuctionService();
                }
            }
        }
        return instance;
    }

    public Auction createAuction(Auction auction) {
        if (auction.getTitle() == null || auction.getTitle().isBlank()) {
            throw new BusinessException("Auction title is required");
        }
        if (auction.getStartingPrice() == null || auction.getStartingPrice().signum() < 0) {
            throw new BusinessException("A non-negative starting price is required");
        }
        auction.setStatus(AuctionStatus.OPEN);
        auction.setWinnerCompanyId(null);
        auction.setWinningAmount(null);
        Auction saved = auctionRepository.save(auction);
        logger.info("Auction created: {} (id={}, starting={})",
                saved.getTitle(), saved.getId(), saved.getStartingPrice());
        return saved;
    }

    public List<Auction> getOpenAuctions() {
        return auctionRepository.findByStatus(AuctionStatus.OPEN);
    }

    public List<Auction> getAllAuctions() {
        return auctionRepository.findAll();
    }

    public Auction getAuction(Long id) {
        return auctionRepository.getById(id);
    }

    public List<Bid> getBids(Long auctionId) {
        return bidRepository.findByAuctionId(auctionId);
    }

    /**
     * Places a bid on behalf of a corporate client. Enforces the NEGOTIATION
     * eligibility rule and the "must beat the current highest" rule.
     */
    public Bid placeBid(Long auctionId, Long companyId, BigDecimal amount) {
        Auction auction = auctionRepository.getById(auctionId);
        if (!auction.isOpen()) {
            throw new BusinessException("Auction is not open for bidding");
        }
        if (amount == null) {
            throw new BusinessException("Bid amount is required");
        }

        Contact company = contactRepository.getById(companyId);
        if (!company.isCorporate()) {
            throw new BusinessException("Only corporate clients can bid");
        }
        if (!hasNegotiationOpportunity(companyId)) {
            throw new BusinessException(
                "Only companies with an opportunity in the NEGOTIATION stage may bid");
        }

        if (amount.compareTo(auction.getStartingPrice()) < 0) {
            throw new BusinessException("Bid must be at least the starting price ("
                    + auction.getStartingPrice() + ")");
        }
        BigDecimal currentHighest = bidRepository.findHighestBid(auctionId)
                .map(Bid::getAmount).orElse(null);
        if (currentHighest != null && amount.compareTo(currentHighest) <= 0) {
            throw new BusinessException("Bid must be higher than the current highest bid ("
                    + currentHighest + ")");
        }

        Bid bid = Bid.builder()
                .auctionId(auctionId)
                .companyId(companyId)
                .companyName(company.getCompanyName())
                .amount(amount)
                .build();
        Bid saved = bidRepository.save(bid);
        logger.info("Bid placed: company={} amount={} auction={}", companyId, amount, auctionId);
        return saved;
    }

    /**
     * Closes the auction and awards it to the highest bidder, if any.
     */
    public Auction closeAuction(Long auctionId) {
        Auction auction = auctionRepository.getById(auctionId);
        if (!auction.isOpen()) {
            throw new BusinessException("Auction is already closed");
        }
        Bid highest = bidRepository.findHighestBid(auctionId).orElse(null);
        if (highest != null) {
            auction.setWinnerCompanyId(highest.getCompanyId());
            auction.setWinningAmount(highest.getAmount());
        }
        auction.setStatus(AuctionStatus.AWARDED);
        Auction saved = auctionRepository.save(auction);
        logger.info("Auction {} closed. Winner company={} amount={}",
                auctionId, auction.getWinnerCompanyId(), auction.getWinningAmount());
        return saved;
    }

    private boolean hasNegotiationOpportunity(Long companyId) {
        List<Opportunity> opps = opportunityRepository.findByClientId(companyId);
        return opps.stream().anyMatch(o -> o.getStage() == OpportunityStage.NEGOTIATION);
    }
}
