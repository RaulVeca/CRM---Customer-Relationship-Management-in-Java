package crm.web.controller;

import crm.facade.CrmFacade;
import crm.model.entity.Invoice;
import crm.web.report.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for invoices generated automatically at each session booking.
 */
@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final CrmFacade facade;
    private final ReportService reports;

    public InvoiceController(CrmFacade facade, ReportService reports) {
        this.facade = facade;
        this.reports = reports;
    }

    /** Admin view: every invoice, newest first. */
    @GetMapping
    public List<Invoice> all() {
        return facade.getAllInvoices();
    }

    @GetMapping("/by-client/{clientId}")
    public List<Invoice> byClient(@PathVariable Long clientId) {
        return facade.getInvoicesForClient(clientId);
    }

    @GetMapping("/by-session/{sessionId}")
    public List<Invoice> bySession(@PathVariable Long sessionId) {
        return facade.getInvoicesForSession(sessionId);
    }

    /** Streams the invoice as a downloadable PDF receipt. */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) {
        Invoice invoice = facade.getInvoice(id);
        byte[] body = reports.invoicePdf(invoice);
        String filename = "invoice-" + invoice.getInvoiceNumber() + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(body);
    }
}
