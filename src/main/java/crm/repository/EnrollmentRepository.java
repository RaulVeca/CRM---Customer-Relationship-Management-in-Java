package crm.repository;

import crm.dao.EnrollmentDao;
import crm.model.entity.Enrollment;
import crm.patterns.GenericDao;

import java.util.List;

public class EnrollmentRepository extends AbstractRepository<Enrollment> {

    private static volatile EnrollmentRepository instance;
    private final EnrollmentDao enrollmentDao;

    private EnrollmentRepository() {
        this.enrollmentDao = EnrollmentDao.getInstance();
    }

    public static EnrollmentRepository getInstance() {
        if (instance == null) {
            synchronized (EnrollmentRepository.class) {
                if (instance == null) {
                    instance = new EnrollmentRepository();
                }
            }
        }
        return instance;
    }

    @Override
    protected GenericDao<Enrollment, Long> getDao() { return enrollmentDao; }

    @Override
    protected String getEntityName() { return "Enrollment"; }

    public List<Enrollment> findByContactId(Long contactId) {
        return enrollmentDao.findByContactId(contactId);
    }

    public List<Enrollment> findBySessionId(Long sessionId) {
        return enrollmentDao.findBySessionId(sessionId);
    }

    public List<Enrollment> findUnpaid() {
        return enrollmentDao.findUnpaid();
    }
}
