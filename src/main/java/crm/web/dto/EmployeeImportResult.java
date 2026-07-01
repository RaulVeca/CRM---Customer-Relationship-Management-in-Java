package crm.web.dto;

import java.util.List;

/**
 * Summary returned after importing employees from an Excel file: how many rows
 * were saved, how many were skipped, and a per-row explanation for every skipped
 * row so the admin can fix the spreadsheet and re-upload.
 */
public record EmployeeImportResult(int imported, int skipped, List<RowError> errors) {

    /** A single spreadsheet row that could not be imported. */
    public record RowError(int row, String message) {}
}
