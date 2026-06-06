package crm.service.opportunity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import crm.exception.BusinessException;
import crm.model.entity.Opportunity;
import crm.model.enums.OpportunityStage;
import crm.observer.EventBus;
import crm.observer.events.OpportunityStageChangedEvent;
import crm.repository.OpportunityRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * SINGLETON - OpportunityService pentru pipeline B2B.
 */
public class OpportunityService {

    private static final Logger logger = LoggerFactory.getLogger(OpportunityService.class);
    private static volatile OpportunityService instance;

    private final OpportunityRepository opportunityRepository;
    private final EventBus eventBus;

    private OpportunityService() {
        this.opportunityRepository = OpportunityRepository.getInstance();
        this.eventBus = EventBus.getInstance();
    }

    public static OpportunityService getInstance() {
        if (instance == null) {
            synchronized (OpportunityService.class) {
                if (instance == null) {
                    instance = new OpportunityService();
                }
            }
        }
        return instance;
    }

    public Opportunity createOpportunity(Opportunity opp) {
        if (opp.getClientId() == null) {
            throw new BusinessException("Client ID este obligatoriu");
        }
        if (opp.getTitle() == null || opp.getTitle().isEmpty()) {
            throw new BusinessException("Titlul este obligatoriu");
        }

        if (opp.getStage() == null) {
            opp.setStage(OpportunityStage.LEAD_QUALIFICATION);
        }
        if (opp.getProbabilityPercent() == null) {
            opp.setProbabilityPercent(opp.getStage().getDefaultProbability());
        }

        Opportunity saved = opportunityRepository.save(opp);
        logger.info("Oportunitate creată: {} (client={}, valoare={})", 
                saved.getTitle(), saved.getClientId(), saved.getEstimatedValue());
        return saved;
    }

    /**
     * Mutare oportunitate într-o nouă etapă cu notificare.
     */
    public void moveToStage(Long opportunityId, OpportunityStage newStage) {
        Opportunity opp = opportunityRepository.getById(opportunityId);
        OpportunityStage oldStage = opp.getStage();

        if (oldStage == newStage) return;

        opp.setStage(newStage);
        opp.setProbabilityPercent(newStage.getDefaultProbability());

        if (newStage == OpportunityStage.WON || newStage == OpportunityStage.LOST) {
            opp.setActualCloseDate(LocalDate.now());
        }

        opportunityRepository.save(opp);
        eventBus.publish(new OpportunityStageChangedEvent(opp, oldStage, newStage, "OpportunityService"));

        logger.info("Oportunitate {} mutată: {} -> {}", opportunityId, oldStage, newStage);
    }

    public void markAsLost(Long opportunityId, String reason) {
        Opportunity opp = opportunityRepository.getById(opportunityId);
        opp.setLostReason(reason);
        opportunityRepository.save(opp);
        moveToStage(opportunityId, OpportunityStage.LOST);
    }

    public Opportunity getById(Long id) {
        return opportunityRepository.getById(id);
    }

    public List<Opportunity> getActivePipeline() {
        return opportunityRepository.findActivePipeline();
    }

    public List<Opportunity> getByStage(OpportunityStage stage) {
        return opportunityRepository.findByStage(stage);
    }

    public List<Opportunity> getByClient(Long clientId) {
        return opportunityRepository.findByClientId(clientId);
    }
}
