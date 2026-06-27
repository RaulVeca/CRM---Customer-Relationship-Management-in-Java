package crm.web.report;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * Downloadable admin reports for contacts and the sales pipeline, in CSV, Excel
 * (.xlsx) or PDF. Each endpoint streams the file with a Content-Disposition
 * header so the browser downloads it directly.
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reports;

    public ReportController(ReportService reports) {
        this.reports = reports;
    }

    @GetMapping("/contacts")
    public ResponseEntity<byte[]> contacts(@RequestParam(defaultValue = "csv") String format) {
        return build(reports.contactsReport(), format, "contacts");
    }

    @GetMapping("/pipeline")
    public ResponseEntity<byte[]> pipeline(@RequestParam(defaultValue = "csv") String format) {
        return build(reports.pipelineReport(), format, "pipeline");
    }

    private ResponseEntity<byte[]> build(ReportService.ReportData data, String format, String baseName) {
        byte[] body;
        MediaType contentType;
        String extension;

        switch (format.toLowerCase()) {
            case "csv" -> {
                body = reports.toCsv(data);
                contentType = MediaType.parseMediaType("text/csv");
                extension = "csv";
            }
            case "xlsx", "excel" -> {
                body = reports.toXlsx(data);
                contentType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                extension = "xlsx";
            }
            case "pdf" -> {
                body = reports.toPdf(data);
                contentType = MediaType.APPLICATION_PDF;
                extension = "pdf";
            }
            default -> throw new ResponseStatusException(BAD_REQUEST, "Unsupported format: " + format);
        }

        String filename = baseName + "-report." + extension;
        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(body);
    }
}
