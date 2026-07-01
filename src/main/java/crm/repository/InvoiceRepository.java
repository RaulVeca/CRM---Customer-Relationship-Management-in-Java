package crm.repository;

import crm.dao.InvoiceDao;
import crm.model.entity.Invoice;
import crm.patterns.GenericDao;

import java.util.List;

/**
 * REPOSITORY - Invoice. Oglindește {@link EnrollmentRepository}.
 */
public class InvoiceRepository extends AbstractRepository<Invoice> {

    private static volatile InvoiceRepository instance;
    private final InvoiceDao invoiceDao;

    private InvoiceRepository() {
        this.invoiceDao = InvoiceDao.getInstance();
    }

    public static InvoiceRepository getInstance() {
        if (instance == null) {
            synchronized (InvoiceRepository.class) {
                if (instance == null) {
                    instance = new InvoiceRepository();
                }
            }
        }
        return instance;
    }

    @Override
    protected GenericDao<Invoice, Long> getDao() { return invoiceDao; }

    @Override
    protected String getEntityName() { return "Invoice"; }

    public List<Invoice> findBySessionId(Long sessionId) {
        return invoiceDao.findBySessionId(sessionId);
    }

    public List<Invoice> findByClientId(Long clientId) {
        return invoiceDao.findByClientId(clientId);
    }
}
