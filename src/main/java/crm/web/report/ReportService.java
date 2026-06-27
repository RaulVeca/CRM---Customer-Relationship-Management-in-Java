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
