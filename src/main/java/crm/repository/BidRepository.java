package crm.repository;

import crm.dao.BidDao;
import crm.model.entity.Bid;
import crm.patterns.GenericDao;

import java.util.List;
import java.util.Optional;

/**
 * SINGLETON + REPOSITORY PATTERN - access to auction bids.
 */
public class BidRepository extends AbstractRepository<Bid> {

    private static volatile BidRepository instance;
    private final BidDao bidDao;

    private BidRepository() {
        this.bidDao = BidDao.getInstance();
    }

    public static BidRepository getInstance() {
        if (instance == null) {
            synchronized (BidRepository.class) {
                if (instance == null) {
                    instance = new BidRepository();
                }
            }
        }
        return instance;
    }

    @Override
    protected GenericDao<Bid, Long> getDao() {
        return bidDao;
    }

    @Override
    protected String getEntityName() {
        return "Bid";
    }

    public List<Bid> findByAuctionId(Long auctionId) {
        return bidDao.findByAuctionId(auctionId);
    }

    public Optional<Bid> findHighestBid(Long auctionId) {
        return bidDao.findHighestBid(auctionId);
    }
}
