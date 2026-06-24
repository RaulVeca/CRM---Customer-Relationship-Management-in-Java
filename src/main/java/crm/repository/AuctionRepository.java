package crm.repository;

import crm.dao.AuctionDao;
import crm.model.entity.Auction;
import crm.model.enums.AuctionStatus;
import crm.patterns.GenericDao;

import java.util.List;

/**
 * SINGLETON + REPOSITORY PATTERN - access to course auctions.
 */
public class AuctionRepository extends AbstractRepository<Auction> {

    private static volatile AuctionRepository instance;
    private final AuctionDao auctionDao;

    private AuctionRepository() {
        this.auctionDao = AuctionDao.getInstance();
    }

    public static AuctionRepository getInstance() {
        if (instance == null) {
            synchronized (AuctionRepository.class) {
                if (instance == null) {
                    instance = new AuctionRepository();
                }
            }
        }
        return instance;
    }

    @Override
    protected GenericDao<Auction, Long> getDao() {
        return auctionDao;
    }

    @Override
    protected String getEntityName() {
        return "Auction";
    }

    public List<Auction> findByStatus(AuctionStatus status) {
        return auctionDao.findByStatus(status);
    }
}
