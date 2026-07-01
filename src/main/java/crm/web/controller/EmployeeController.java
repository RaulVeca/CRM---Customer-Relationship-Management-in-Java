package crm.web.controller;

import crm.facade.CrmFacade;
import crm.model.entity.Employee;
import crm.web.dto.EmployeeImportResult;
import crm.web.dto.EmployeeImportResult.RowError;
import crm.web.employee.EmployeeExcelImporter;
import crm.web.employee.EmployeeExcelImporter.ParsedRow;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
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

    /**
     * Bulk-imports employees from an uploaded Excel file into the given company.
     * Every valid row is saved to the {@code employees} table; invalid rows are
     * skipped and reported back with their row number so the admin can correct
     * the spreadsheet. Reached only from the (admin-gated) portal.
     */
    @PostMapping("/import")
    public EmployeeImportResult importExcel(@RequestParam("companyId") Long companyId,
                                            @RequestParam("file") MultipartFile file) {
        if (companyId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "companyId is required");
        }
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No file uploaded");
        }

        List<ParsedRow> parsed;
        try {
            parsed = EmployeeExcelImporter.parse(file.getInputStream());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Could not read the Excel file: " + e.getMessage());
        }

        int imported = 0;
        int skipped = 0;
        List<RowError> errors = new ArrayList<>();
        for (ParsedRow row : parsed) {
            if (row.error() != null) {
                skipped++;
                errors.add(new RowError(row.rowNumber(), row.error()));
                continue;
            }
            try {
                Employee e = row.employee();
                e.setCompanyId(companyId);
                facade.addEmployee(e);
                imported++;
            } catch (Exception ex) {
                skipped++;
                errors.add(new RowError(row.rowNumber(), ex.getMessage()));
            }
        }
        return new EmployeeImportResult(imported, skipped, errors);
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
