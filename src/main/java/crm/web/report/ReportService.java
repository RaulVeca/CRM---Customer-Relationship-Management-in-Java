package crm.web.report;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import crm.facade.CrmFacade;
import crm.model.entity.Contact;
import crm.model.entity.Invoice;
import crm.model.entity.Opportunity;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Builds downloadable reports (CSV / Excel / PDF) for the admin section.
 *
 * <p>Each dataset is reduced to a generic {@link ReportData} (a title + headers +
 * string rows) so the three format writers stay completely independent of the
 * domain model.
 */
@Service
public class ReportService {

    /** Generic tabular report payload shared by all format writers. */
    public record ReportData(String title, List<String> headers, List<List<String>> rows) {}

    private static final int MAX_ROWS = 10_000;

    private final CrmFacade facade;

    public ReportService(CrmFacade facade) {
        this.facade = facade;
    }

    // =====================================================
    // Datasets
    // =====================================================

    public ReportData contactsReport() {
        List<Contact> contacts = facade.getAllContacts(0, MAX_ROWS);
        List<String> headers = List.of("ID", "Type", "Name", "Email", "Phone", "Industry", "Lead status", "Lead score");
        List<List<String>> rows = new ArrayList<>();
        for (Contact c : contacts) {
            rows.add(List.of(
                    str(c.getId()),
                    c.getContactType() == null ? "-" : c.getContactType().name(),
                    contactName(c),
                    str(c.getEmail()),
                    str(c.getPhone()),
                    str(c.getIndustry()),
                    c.getLeadStatus() == null ? "-" : c.getLeadStatus().name(),
                    str(c.getLeadScore())
            ));
        }
        return new ReportData("Contacts report", headers, rows);
    }

    public ReportData pipelineReport() {
        List<Opportunity> pipeline = facade.getActivePipeline();
        Map<Long, String> clientNames = facade.getAllContacts(0, MAX_ROWS).stream()
                .collect(Collectors.toMap(Contact::getId, this::contactName, (a, b) -> a));

        List<String> headers = List.of("ID", "Title", "Stage", "Client", "Estimated value",
                "Quoted value", "Probability %", "Participants");
        List<List<String>> rows = new ArrayList<>();
        for (Opportunity o : pipeline) {
            rows.add(List.of(
                    str(o.getId()),
                    str(o.getTitle()),
                    o.getStage() == null ? "-" : o.getStage().getLabel(),
                    o.getClientId() == null ? "-" : clientNames.getOrDefault(o.getClientId(), "#" + o.getClientId()),
                    str(o.getEstimatedValue()),
                    str(o.getQuotedValue()),
                    str(o.getProbabilityPercent()),
                    str(o.getEstimatedParticipants())
            ));
        }
        return new ReportData("Sales pipeline report", headers, rows);
    }

    // =====================================================
    // Format writers
    // =====================================================

    public byte[] toCsv(ReportData data) {
        StringBuilder sb = new StringBuilder("﻿"); // UTF-8 BOM so Excel reads diacritics
        sb.append(data.headers().stream().map(ReportService::csvEscape).collect(Collectors.joining(",")));
        sb.append("\r\n");
        for (List<String> row : data.rows()) {
            sb.append(row.stream().map(ReportService::csvEscape).collect(Collectors.joining(",")));
            sb.append("\r\n");
        }
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    public byte[] toXlsx(ReportData data) {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet(data.title());

            CellStyle headerStyle = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row header = sheet.createRow(0);
            for (int i = 0; i < data.headers().size(); i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(data.headers().get(i));
                cell.setCellStyle(headerStyle);
            }

            int r = 1;
            for (List<String> row : data.rows()) {
                Row sheetRow = sheet.createRow(r++);
                for (int i = 0; i < row.size(); i++) {
                    sheetRow.createCell(i).setCellValue(row.get(i));
                }
            }
            for (int i = 0; i < data.headers().size(); i++) {
                sheet.autoSizeColumn(i);
            }

            wb.write(out);
            return out.toByteArray();
        } catch (java.io.IOException e) {
            throw new UncheckedIOException("Could not build XLSX report", e);
        }
    }

    public byte[] toPdf(ReportData data) {
        Document doc = new Document(PageSize.A4.rotate(), 28, 28, 28, 28);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, out);
        doc.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Paragraph title = new Paragraph(data.title(), titleFont);
        title.setSpacingAfter(12f);
        doc.add(title);

        PdfPTable table = new PdfPTable(data.headers().size());
        table.setWidthPercentage(100);

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
        for (String h : data.headers()) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(new Color(79, 70, 229)); // indigo-600
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setPadding(5f);
            table.addCell(cell);
        }

        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 8);
        boolean stripe = false;
        for (List<String> row : data.rows()) {
            Color bg = stripe ? new Color(241, 245, 249) : Color.WHITE; // slate-100 stripes
            for (String value : row) {
                PdfPCell cell = new PdfPCell(new Phrase(value, bodyFont));
                cell.setBackgroundColor(bg);
                cell.setPadding(4f);
                table.addCell(cell);
            }
            stripe = !stripe;
        }

        doc.add(table);
        doc.close();
        return out.toByteArray();
    }

    // =====================================================
    // Single invoice (receipt) PDF
    // =====================================================

    /** Fixed seller / bank details, mirroring the payment window on the booking page. */
    private static final String COMPANY_NAME = "TrainingIT SRL";
    private static final String BANK_NAME = "Banca Transilvania";
    private static final String BANK_IBAN = "RO49 BTRL 0000 1234 5678 9012";

    /**
     * Renders a single session-booking invoice as a proper one-page PDF receipt:
     * seller block, bill-to, the session line item priced at the hourly rate, the
     * employee discount (when any) and the paid total.
     */
    public byte[] invoicePdf(Invoice inv) {
        Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, out);
        doc.open();

        Font h1 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, new Color(79, 70, 229));
        Font label = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new Color(100, 116, 139));
        Font body = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font bodyBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

        // Header: title + invoice number / dates
        PdfPTable head = new PdfPTable(2);
        head.setWidthPercentage(100);
        head.setWidths(new int[] {1, 1});
        head.addCell(borderless(new Phrase("INVOICE", h1), Element.ALIGN_LEFT));
        PdfPCell meta = new PdfPCell();
        meta.setBorder(0);
        meta.setHorizontalAlignment(Element.ALIGN_RIGHT);
        meta.addElement(right(new Phrase(str(inv.getInvoiceNumber()), bodyBold)));
        meta.addElement(right(new Phrase("Issue date: " + str(inv.getIssueDate()), body)));
        meta.addElement(right(new Phrase("Status: " + str(inv.getStatus()), body)));
        if (inv.getPaymentDate() != null) {
            meta.addElement(right(new Phrase("Paid on: " + str(inv.getPaymentDate()), body)));
        }
        head.addCell(meta);
        doc.add(head);

        doc.add(spacer(16f));

        // Seller + Bill-to
        PdfPTable parties = new PdfPTable(2);
        parties.setWidthPercentage(100);
        parties.setWidths(new int[] {1, 1});
        parties.addCell(partyCell("FROM", List.of(COMPANY_NAME, BANK_NAME, "IBAN: " + BANK_IBAN), label, body));
        parties.addCell(partyCell("BILL TO", List.of(str(inv.getClientEmail())), label, body));
        doc.add(parties);

        doc.add(spacer(18f));

        // Line items
        PdfPTable items = new PdfPTable(4);
        items.setWidthPercentage(100);
        items.setWidths(new int[] {6, 2, 2, 2});
        Font thFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
        for (String h : List.of("Description", "Hours", "Unit price", "Amount")) {
            PdfPCell c = new PdfPCell(new Phrase(h, thFont));
            c.setBackgroundColor(new Color(79, 70, 229));
            c.setPadding(6f);
            c.setHorizontalAlignment(h.equals("Description") ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT);
            items.addCell(c);
        }
        items.addCell(bodyCell(str(inv.getDescription()), body, Element.ALIGN_LEFT));
        items.addCell(bodyCell(String.valueOf(inv.getHours()), body, Element.ALIGN_RIGHT));
        items.addCell(bodyCell(money(inv.getHourlyRate()), body, Element.ALIGN_RIGHT));
        items.addCell(bodyCell(money(inv.getSubtotal()), body, Element.ALIGN_RIGHT));
        doc.add(items);

        // Totals
        PdfPTable totals = new PdfPTable(2);
        totals.setWidthPercentage(45);
        totals.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totals.setWidths(new int[] {3, 2});
        totals.setSpacingBefore(10f);
        totalRow(totals, "Subtotal", money(inv.getSubtotal()), body, body);
        if (inv.getDiscountRate() != null && inv.getDiscountRate().signum() > 0) {
            String pct = inv.getDiscountRate().multiply(java.math.BigDecimal.valueOf(100)).intValue() + "%";
            totalRow(totals, "Employee discount (" + pct + ")", "-" + money(inv.getDiscountAmount()), body, body);
        }
        totalRow(totals, "Total", money(inv.getTotal()), bodyBold, bodyBold);
        doc.add(totals);

        Paragraph note = new Paragraph("Thank you. This invoice was paid in full.",
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, new Color(100, 116, 139)));
        note.setSpacingBefore(24f);
        doc.add(note);

        doc.close();
        return out.toByteArray();
    }

    private PdfPCell partyCell(String heading, List<String> lines, Font label, Font body) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(0);
        cell.addElement(new Phrase(heading, label));
        for (String line : lines) {
            cell.addElement(new Phrase(str(line), body));
        }
        return cell;
    }

    private void totalRow(PdfPTable t, String label, String value, Font lf, Font vf) {
        PdfPCell l = new PdfPCell(new Phrase(label, lf));
        l.setBorder(0);
        l.setPadding(4f);
        PdfPCell v = new PdfPCell(new Phrase(value, vf));
        v.setBorder(0);
        v.setPadding(4f);
        v.setHorizontalAlignment(Element.ALIGN_RIGHT);
        t.addCell(l);
        t.addCell(v);
    }

    private PdfPCell borderless(Phrase p, int align) {
        PdfPCell c = new PdfPCell(p);
        c.setBorder(0);
        c.setHorizontalAlignment(align);
        return c;
    }

    private Paragraph right(Phrase p) {
        Paragraph par = new Paragraph(p);
        par.setAlignment(Element.ALIGN_RIGHT);
        return par;
    }

    private PdfPCell bodyCell(String value, Font font, int align) {
        PdfPCell c = new PdfPCell(new Phrase(value, font));
        c.setPadding(6f);
        c.setHorizontalAlignment(align);
        return c;
    }

    private Paragraph spacer(float height) {
        Paragraph p = new Paragraph(" ");
        p.setSpacingAfter(height);
        return p;
    }

    /** Formats a money amount as {@code $12.00} (USD, two decimals). */
    private static String money(java.math.BigDecimal amount) {
        java.math.BigDecimal v = amount == null ? java.math.BigDecimal.ZERO : amount;
        return "$" + v.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    // =====================================================
    // Helpers
    // =====================================================

    private String contactName(Contact c) {
        if (c.isCorporate()) {
            return c.getCompanyName() == null ? "(company)" : c.getCompanyName();
        }
        String name = (str(c.getFirstName()) + " " + str(c.getLastName())).trim();
        return name.isEmpty() ? "(no name)" : name;
    }

    private static String str(Object o) {
        return o == null ? "" : o.toString();
    }

    private static String csvEscape(String value) {
        String v = value == null ? "" : value;
        if (v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r")) {
            return "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }
}
