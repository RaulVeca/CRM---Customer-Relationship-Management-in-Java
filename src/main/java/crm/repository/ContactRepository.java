package crm.repository;

import crm.dao.ContactDao;
import crm.model.entity.Contact;
import crm.model.enums.LeadStatus;
import crm.patterns.GenericDao;

import java.util.List;
import java.util.Optional;

/**
 * SINGLETON + REPOSITORY PATTERN
 * 
 * ContactRepository - clasa centrală pentru acces la date despre contacte.
 * Este Singleton pentru a garanta o singură instanță în aplicație.
 * 
 * Diferența față de DAO:
 * - DAO oferă operații CRUD pe tabela
 * - Repository oferă operații business-oriented
 */
public class ContactRepository extends AbstractRepository<Contact> {

    private static volatile ContactRepository instance;
    private final ContactDao contactDao;

    private ContactRepository() {
        this.contactDao = ContactDao.getInstance();
    }

    public static ContactRepository getInstance() {
        if (instance == null) {
            synchronized (ContactRepository.class) {
                if (instance == null) {
                    instance = new ContactRepository();
                }
            }
        }
        return instance;
    }

    @Override
    protected GenericDao<Contact, Long> getDao() {
        return contactDao;
    }

    @Override
    protected String getEntityName() {
        return "Contact";
    }

    /**
     * Caută un contact după email.
     */
    public Optional<Contact> findByEmail(String email) {
        return contactDao.findByEmail(email);
    }

    /**
     * Returnează lead-urile cu un anumit status.
     */
    public List<Contact> findByLeadStatus(LeadStatus status) {
        return contactDao.findByLeadStatus(status);
    }

    /**
     * Returnează lead-urile fierbinți (cu scor >= prag).
     */
    public List<Contact> findHotLeads(int minScore, int limit) {
        return contactDao.findHotLeads(minScore, limit);
    }

    /**
     * Caută contacte folosind un termen de căutare.
     */
    public List<Contact> search(String searchTerm, int offset, int limit) {
        return contactDao.search(searchTerm, offset, limit);
    }

    /**
     * Statistici - număr contacte pe status.
     */
    public long countByLeadStatus(LeadStatus status) {
        return contactDao.countByLeadStatus(status);
    }

    /**
     * Verifică dacă există un contact cu acest email.
     */
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }
}
