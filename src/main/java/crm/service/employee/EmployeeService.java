package crm.service.employee;

import crm.exception.BusinessException;
import crm.model.entity.Employee;
import crm.repository.ContactRepository;
import crm.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * SINGLETON - business logic for managing the employees of corporate clients.
 */
public class EmployeeService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);
    private static volatile EmployeeService instance;

    private final EmployeeRepository employeeRepository;
    private final ContactRepository contactRepository;

    private EmployeeService() {
        this.employeeRepository = EmployeeRepository.getInstance();
        this.contactRepository = ContactRepository.getInstance();
    }

    public static EmployeeService getInstance() {
        if (instance == null) {
            synchronized (EmployeeService.class) {
                if (instance == null) {
                    instance = new EmployeeService();
                }
            }
        }
        return instance;
    }

    public Employee addEmployee(Employee employee) {
        if (employee.getCompanyId() == null) {
            throw new BusinessException("Company ID is required for an employee");
        }
        // Ensure the company exists and is a corporate client.
        var company = contactRepository.getById(employee.getCompanyId());
        if (!company.isCorporate()) {
            throw new BusinessException("Employees can only be attached to a CORPORATE contact");
        }
        if ((employee.getFirstName() == null || employee.getFirstName().isBlank())
                && (employee.getLastName() == null || employee.getLastName().isBlank())) {
            throw new BusinessException("Employee name is required");
        }
        Employee saved = employeeRepository.save(employee);
        logger.info("Employee added: {} (company={})", saved.getFullName(), saved.getCompanyId());
        return saved;
    }

    public Employee updateEmployee(Employee employee) {
        if (employee.getId() == null) {
            throw new BusinessException("Employee ID is required for update");
        }
        employeeRepository.getById(employee.getId()); // verify existence
        return employeeRepository.save(employee);
    }

    public Employee getById(Long id) {
        return employeeRepository.getById(id);
    }

    public List<Employee> getByCompany(Long companyId) {
        return employeeRepository.findByCompanyId(companyId);
    }

    public boolean deleteEmployee(Long id) {
        return employeeRepository.deleteById(id);
    }
}
