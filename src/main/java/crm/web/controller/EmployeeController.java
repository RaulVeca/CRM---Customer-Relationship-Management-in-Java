package crm.web.controller;

import crm.facade.CrmFacade;
import crm.model.entity.Employee;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for the employees of corporate clients. Their work and
 * interest profiles feed the AI course-recommendation engine.
 */
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final CrmFacade facade;

    public EmployeeController(CrmFacade facade) {
        this.facade = facade;
    }

    @GetMapping("/by-company/{companyId}")
    public List<Employee> byCompany(@PathVariable Long companyId) {
        return facade.getEmployeesForCompany(companyId);
    }

    @GetMapping("/{id}")
    public Employee getById(@PathVariable Long id) {
        return facade.getEmployee(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Employee create(@RequestBody Employee employee) {
        return facade.addEmployee(employee);
    }

    @PutMapping("/{id}")
    public Employee update(@PathVariable Long id, @RequestBody Employee employee) {
        employee.setId(id);
        return facade.updateEmployee(employee);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = facade.deleteEmployee(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
