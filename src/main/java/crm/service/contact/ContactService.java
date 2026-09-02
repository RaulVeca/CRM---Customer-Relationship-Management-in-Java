package crm.service.contact;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import crm.config.DatabaseConnection;
import crm.exception.BusinessException;
import crm.model.entity.Contact;
import crm.model.enums.LeadStatus;
import crm.observer.EventBus;
import crm.observer.events.ContactCreatedEvent;
import crm.observer.events.LeadStatusChangedEvent;
import crm.repository.ContactRepository;
import crm.strategy.LeadScoringContext;
import crm.validation.ContactValidatorChain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SINGLETON PATTERN - ContactService
 * 
 * Layer-ul de logică business pentru gestionarea contactelor.
 * Folosește:
 * - REPOSITORY pentru acces date
 * - STRATEGY pentru calcul scor
 * - OBSERVER pentru notificare evenimente
 * - CHAIN OF RESPONSIBILITY pentru validare
 */
public class ContactService {

    private static final Logger logger = LoggerFactory.getLogger(ContactService.class);
    private static volatile ContactService instance;

    private final ContactRepository contactRepository;
    private final ContactValidatorChain validator;
    private final EventBus eventBus;

    private ContactService() {
        this.contactRepository = ContactRepository.getInstance();
        this.validator = new ContactValidatorChain();
        this.eventBus = EventBus.getInstance();
    }

    public static ContactService getInstance() {
        if (instance == null) {
            synchronized (ContactService.class) {
                if (instance == null) {
                    instance = new ContactService();
                }
            }
        }
        return instance;
    }

    /**
     * Creează un contact nou cu validare, scoring și notificare.
     */
    public Contact createContact(Contact contact) {
        logger.info("Creare contact: {}", contact.getEmail());

        // Validare
        validator.validateAndThrow(contact);

        // Verificare duplicate
        if (contactRepository.existsByEmail(contact.getEmail())) {
            throw new BusinessException("Email-ul " + contact.getEmail() + " is already registered");
        }

        // Inițializare valori implicite
        if (contact.getLeadStatus() == null) {
            contact.setLeadStatus(LeadStatus.NEW);
        }
        if (contact.getFirstContactDate() == null) {
            contact.setFirstContactDate(LocalDateTime.now());
        }
        contact.setLastContactDate(LocalDateTime.now());

        // STRATEGY PATTERN - calcul scor inițial
        LeadScoringContext scoringContext = new LeadScoringContext(contact);
        contact.setLeadScore(scoringContext.calculateInitialScore(contact));

        // Salvare
        Contact saved = contactRepository.save(contact);

        // OBSERVER PATTERN - notificare eveniment
        eventBus.publish(new ContactCreatedEvent(saved, "ContactService"));

        logger.info("Contact created with ID: {} (score: {})", saved.getId(), saved.getLeadScore());
        return saved;
    }

    /**
     * Actualizează un contact existent.
     */
    public Contact updateContact(Contact contact) {
        if (contact.getId() == null) {
            throw new BusinessException("Contact ID is required for update");
        }

        Contact existing = contactRepository.getById(contact.getId());
        validator.validateAndThrow(contact);

        // Verifică schimbarea email-ului
        if (!existing.getEmail().equals(contact.getEmail())) {
            if (contactRepository.existsByEmail(contact.getEmail())) {
                throw new BusinessException("Email-ul " + contact.getEmail() + " is already in use");
            }
        }

        return contactRepository.save(contact);
    }

    /**
     * Schimbă statusul unui lead și notifică observatori.
     */
    public void updateLeadStatus(Long contactId, LeadStatus newStatus) {
        Contact contact = contactRepository.getById(contactId);
        LeadStatus oldStatus = contact.getLeadStatus();

        if (oldStatus == newStatus) return;

        contact.setLeadStatus(newStatus);
        contact.setLastContactDate(LocalDateTime.now());
        contactRepository.save(contact);

        eventBus.publish(new LeadStatusChangedEvent(contact, oldStatus, newStatus, "ContactService"));
        logger.info("Status schimbat pentru contact {}: {} -> {}", contactId, oldStatus, newStatus);
    }

    /**
     * Actualizează scorul lead-ului în funcție de o activitate.
     */
    public void updateLeadScoreFromActivity(Long contactId, String activityType) {
        Contact contact = contactRepository.getById(contactId);

        LeadScoringContext scoringContext = new LeadScoringContext(contact);
        int impact = scoringContext.calculateActivityImpact(activityType);

        if (impact == 0) return;

        int oldScore = contact.getLeadScore() != null ? contact.getLeadScore() : 0;
        int newScore = Math.max(0, Math.min(100, oldScore + impact));

        contact.setLeadScore(newScore);
        contactRepository.save(contact);

        logScoreChange(contactId, impact, activityType, oldScore, newScore);
        logger.info("Scor actualizat pentru contact {}: {} -> {} ({})", 
                contactId, oldScore, newScore, activityType);
    }

    private void logScoreChange(Long contactId, int change, String reason, int oldScore, int newScore) {
        String sql = "INSERT INTO lead_score_logs (contact_id, score_change, reason, " +
                     "previous_score, new_score) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contactId);
            ps.setInt(2, change);
            ps.setString(3, reason);
            ps.setInt(4, oldScore);
            ps.setInt(5, newScore);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error logging score", e);
        }
    }

    public Optional<Contact> findById(Long id) {
        return contactRepository.findById(id);
    }

    public Contact getById(Long id) {
        return contactRepository.getById(id);
    }

    public Optional<Contact> findByEmail(String email) {
        return contactRepository.findByEmail(email);
    }

    public List<Contact> getHotLeads(int limit) {
        return contactRepository.findHotLeads(70, limit);
    }

    public List<Contact> getAllContacts() {
        return contactRepository.findAll();
    }

    public List<Contact> getContactsPage(int page, int pageSize) {
        return contactRepository.findAll(page * pageSize, pageSize);
    }

    public List<Contact> searchContacts(String query, int page, int pageSize) {
        return contactRepository.search(query, page * pageSize, pageSize);
    }

    public List<Contact> getContactsByStatus(LeadStatus status) {
        return contactRepository.findByLeadStatus(status);
    }

    public boolean deleteContact(Long id) {
        return contactRepository.deleteById(id);
    }

    public long countTotal() {
        return contactRepository.count();
    }

    public long countByStatus(LeadStatus status) {
        return contactRepository.countByLeadStatus(status);
    }
}
