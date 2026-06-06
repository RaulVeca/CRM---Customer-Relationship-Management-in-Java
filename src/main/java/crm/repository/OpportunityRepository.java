package crm.repository;

import crm.dao.OpportunityDao;
import crm.model.entity.Opportunity;
import crm.model.enums.OpportunityStage;
import crm.patterns.GenericDao;

import java.util.List;

public class OpportunityRepository extends AbstractRepository<Opportunity> {

    private static volatile OpportunityRepository instance;
    private final OpportunityDao opportunityDao;

    private OpportunityRepository() {
        this.opportunityDao = OpportunityDao.getInstance();
    }

    public static OpportunityRepository getInstance() {
        if (instance == null) {
            synchronized (OpportunityRepository.class) {
                if (instance == null) {
                    instance = new OpportunityRepository();
                }
            }
        }
        return instance;
    }

    @Override
    protected GenericDao<Opportunity, Long> getDao() { return opportunityDao; }

    @Override
    protected String getEntityName() { return "Opportunity"; }

    public List<Opportunity> findByClientId(Long clientId) {
        return opportunityDao.findByClientId(clientId);
    }

    public List<Opportunity> findByStage(OpportunityStage stage) {
        return opportunityDao.findByStage(stage);
    }

    public List<Opportunity> findActivePipeline() {
        return opportunityDao.findActivePipeline();
    }
}
