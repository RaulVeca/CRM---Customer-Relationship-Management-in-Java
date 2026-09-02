package crm.repository;

import crm.dao.EmployeeDao;
import crm.model.entity.Employee;
import crm.patterns.GenericDao;

import java.util.List;

/**
 * SINGLETON + REPOSITORY PATTERN - business-oriented access to employees.
 */
public class EmployeeRepository extends AbstractRepository<Employee> {

    private static volatile EmployeeRepository instance;
    private final EmployeeDao employeeDao;

    private EmployeeRepository() {
        this.employeeDao = EmployeeDao.getInstance();
    }

    public static EmployeeRepository getInstance() {
        if (instance == null) {
            synchronized (EmployeeRepository.class) {
                if (instance == null) {
                    instance = new EmployeeRepository();
                }
            }
        }
        return instance;
    }

    @Override
    protected GenericDao<Employee, Long> getDao() {
        return employeeDao;
    }

    @Override
    protected String getEntityName() {
        return "Employee";
    }

    public List<Employee> findByCompanyId(Long companyId) {
        return employeeDao.findByCompanyId(companyId);
    }
}
