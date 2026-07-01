package crm.web.employee;

import crm.model.entity.Employee;
import crm.model.enums.ProfileArea;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses an uploaded Excel workbook (the "Employees" sheet layout produced for
 * the admin portal) into {@link Employee} rows, resolving the human profile
 * labels back to {@link ProfileArea} constants.
 *
 * <p>Column order does not matter — the header row is read and columns are
 * matched by (case-insensitive) name, so extra columns such as {@code Company}
 * are simply ignored. The company an employee belongs to is decided by the
 * caller (the selected company in the admin portal), not by the spreadsheet.</p>
 *
 * <p>Parsing never throws on a bad data row: each row is returned as a
 * {@link ParsedRow} that carries either a ready {@link Employee} or an
 * {@code error} message, so the caller can report exactly which rows failed.</p>
 */
public final class EmployeeExcelImporter {

    /** One spreadsheet data row: its 1-based number and either an employee or an error. */
    public record ParsedRow(int rowNumber, Employee employee, String error) {}

    private static final DataFormatter FORMATTER = new DataFormatter();

    private EmployeeExcelImporter() {
    }

    /**
     * Reads the first sheet of the workbook. The {@code companyId} is left unset
     * on the returned employees — the caller assigns it before saving.
     */
    public static List<ParsedRow> parse(InputStream in) throws IOException {
        List<ParsedRow> rows = new ArrayList<>();
        try (Workbook wb = WorkbookFactory.create(in)) {
            Sheet sheet = wb.getNumberOfSheets() == 0 ? null : wb.getSheetAt(0);
            if (sheet == null) return rows;

            Row header = sheet.getRow(sheet.getFirstRowNum());
            if (header == null) return rows;
            Map<String, Integer> cols = mapColumns(header);

            int last = sheet.getLastRowNum();
            for (int r = header.getRowNum() + 1; r <= last; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String firstName = cell(row, cols.get("firstName"));
                String lastName = cell(row, cols.get("lastName"));
                String email = cell(row, cols.get("email"));
                String jobTitle = cell(row, cols.get("jobTitle"));
                String workProfileRaw = cell(row, cols.get("workProfile"));
                String interestsRaw = cell(row, cols.get("interestProfiles"));

                // Fully blank line → skip silently (trailing empty rows are common).
                if (isBlank(firstName) && isBlank(lastName) && isBlank(email)
                        && isBlank(jobTitle) && isBlank(workProfileRaw) && isBlank(interestsRaw)) {
                    continue;
                }

                int humanRow = r + 1; // Excel-style 1-based row number

                if (isBlank(firstName) && isBlank(lastName)) {
                    rows.add(new ParsedRow(humanRow, null, "Missing employee name"));
                    continue;
                }

                ProfileArea workProfile = null;
                if (!isBlank(workProfileRaw)) {
                    workProfile = ProfileArea.fromLabelOrName(workProfileRaw);
                    if (workProfile == null) {
                        rows.add(new ParsedRow(humanRow, null,
                                "Unknown work profile: \"" + workProfileRaw.trim() + "\""));
                        continue;
                    }
                }

                Employee e = new Employee();
                e.setFirstName(trimToNull(firstName));
                e.setLastName(trimToNull(lastName));
                e.setEmail(trimToNull(email));
                e.setJobTitle(trimToNull(jobTitle));
                e.setWorkProfile(workProfile);
                e.setInterestProfiles(parseInterests(interestsRaw));

                rows.add(new ParsedRow(humanRow, e, null));
            }
        }
        return rows;
    }

    /** Maps our canonical field keys to the column index of a matching header cell. */
    private static Map<String, Integer> mapColumns(Row header) {
        Map<String, Integer> cols = new HashMap<>();
        for (int c = header.getFirstCellNum(); c < header.getLastCellNum(); c++) {
            String name = cell(header, c);
            if (name == null) continue;
            String key = canonicalField(name);
            if (key != null) cols.putIfAbsent(key, c);
        }
        return cols;
    }

    /** Recognises the various header spellings (English + Romanian) we accept. */
    private static String canonicalField(String header) {
        String h = header.trim().toLowerCase().replaceAll("\\s+", " ");
        return switch (h) {
            case "first name", "firstname", "prenume" -> "firstName";
            case "last name", "lastname", "nume" -> "lastName";
            case "email", "e-mail", "mail" -> "email";
            case "job title", "jobtitle", "job", "role", "functie", "funcție" -> "jobTitle";
            case "work profile", "workprofile", "works in", "profile", "profil" -> "workProfile";
            case "interest profiles", "interests", "interested in", "interese" -> "interestProfiles";
            default -> null; // includes "company" and any unrecognised column
        };
    }

    /** Splits the interests cell on commas/semicolons and resolves known areas; unknown tokens are ignored. */
    private static List<ProfileArea> parseInterests(String raw) {
        List<ProfileArea> out = new ArrayList<>();
        if (isBlank(raw)) return out;
        for (String token : raw.split("[,;]")) {
            ProfileArea area = ProfileArea.fromLabelOrName(token);
            if (area != null && !out.contains(area)) out.add(area);
        }
        return out;
    }

    private static String cell(Row row, Integer index) {
        if (index == null || index < 0) return null;
        Cell c = row.getCell(index);
        if (c == null) return null;
        String v = FORMATTER.formatCellValue(c);
        return v == null ? null : v.trim();
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
