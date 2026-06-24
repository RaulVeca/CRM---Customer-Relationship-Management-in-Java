package crm.web.controller;

import crm.facade.CrmFacade;
import crm.model.entity.Auction;
import crm.model.entity.Bid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST endpoints for course auctions (bidding). Companies in the NEGOTIATION
 * stage place bids; the highest bidder wins when the auction is closed.
 */
@RestController
@RequestMapping("/api/auctions")
public class AuctionController {

    private final CrmFacade facade;

    public AuctionController(CrmFacade facade) {
        this.facade = facade;
    }

    @GetMapping
    public List<Auction> list(@RequestParam(defaultValue = "false") boolean openOnly) {
        return openOnly ? facade.getOpenAuctions() : facade.getAllAuctions();
    }

    @GetMapping("/{id}")
    public Auction getById(@PathVariable Long id) {
        return facade.getAuction(id);
    }

    @GetMapping("/{id}/bids")
    public List<Bid> bids(@PathVariable Long id) {
        return facade.getBids(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Auction create(@RequestBody Auction auction) {
        return facade.createAuction(auction);
    }

    @PostMapping("/{id}/bids")
    @ResponseStatus(HttpStatus.CREATED)
    public Bid placeBid(@PathVariable Long id, @RequestBody BidRequest req) {
        return facade.placeBid(id, req.companyId(), req.amount());
    }

    @PatchMapping("/{id}/close")
    public Auction close(@PathVariable Long id) {
        return facade.closeAuction(id);
    }

    public record BidRequest(Long companyId, BigDecimal amount) {}
}
