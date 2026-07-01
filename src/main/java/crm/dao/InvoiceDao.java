package crm.dao;

import crm.exception.DataAccessException;
import crm.model.entity.Invoice;
import crm.model.enums.PaymentStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TEMPLATE METHOD - InvoiceDao.
 *
 * <p>Persistă facturile generate automat la fiecare rezervare de ședință (booking).
 * Oglindește structura celorlalte DAO-uri (JDBC simplu, fără JPA).</p>
 */
public class InvoiceDao extends AbstractDao<Invoice> {

    private static volatile InvoiceDao instance;

    private InvoiceDao() {}

    public static InvoiceDao getInstance() {
        if (instance == null) {
            synchronized (InvoiceDao.class) {
                if (instance == null) {
                    instance = new InvoiceDao();
                }
            }
        }
        return instance;
    }

    @Override
    protected String getTableName() { return "session_invoices"; }

    @Override
    protected String getInsertSql() {
        return "INSERT INTO session_invoices (invoice_number, session_id, client_id, client_email, " +
                "issue_date, hours, hourly_rate, subtotal, discount_rate, discount_amount, " +
                "total, paid_amount, status, payment_date, description) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSql() {
        return "UPDATE session_invoices SET invoice_number=?, session_id=?, client_id=?, client_email=?, " +
                "issue_date=?, hours=?, hourly_rate=?, subtotal=?, discount_rate=?, " +
                "discount_amount=?, total=?, paid_amount=?, status=?, payment_date=?, description=? " +
                "WHERE id=?";
    }

    @Override
    protected void setInsertParameters(PreparedStatement ps, Invoice inv) throws SQLException {
        bindColumns(ps, inv, 1);
    }

    @Override
    protected void setUpdateParameters(PreparedStatement ps, Invoice inv) throws SQLException {
        int next = bindColumns(ps, inv, 1);
        ps.setLong(next, inv.getId());
    }

    /** Binds the shared column list starting at {@code index}; returns the next free index. */
    private int bindColumns(PreparedStatement ps, Invoice inv, int index) throws SQLException {
        int i = index;
        ps.setString(i++, inv.getInvoiceNumber());
        if (inv.getSessionId() == null) ps.setNull(i++, Types.BIGINT);
        else ps.setLong(i++, inv.getSessionId());
        if (inv.getClientId() == null) ps.setNull(i++, Types.BIGINT);
        else ps.setLong(i++, inv.getClientId());
        ps.setString(i++, inv.getClientEmail());
        ps.setDate(i++, inv.getIssueDate() != null ? Date.valueOf(inv.getIssueDate()) : null);
        ps.setInt(i++, inv.getHours());
        ps.setBigDecimal(i++, inv.getHourlyRate());
        ps.setBigDecimal(i++, inv.getSubtotal());
        ps.setBigDecimal(i++, inv.getDiscountRate());
        ps.setBigDecimal(i++, inv.getDiscountAmount());
        ps.setBigDecimal(i++, inv.getTotal());
        ps.setBigDecimal(i++, inv.getPaidAmount());
        ps.setString(i++, inv.getStatus() != null ? inv.getStatus().name() : PaymentStatus.UNPAID.name());
        ps.setDate(i++, inv.getPaymentDate() != null ? Date.valueOf(inv.getPaymentDate()) : null);
        ps.setString(i++, inv.getDescription());
        return i;
    }

    @Override
    protected void setEntityId(Invoice entity, Long id) { entity.setId(id); }

    @Override
    protected Long getEntityId(Invoice entity) { return entity.getId(); }

    @Override
    protected Invoice mapResultSetToEntity(ResultSet rs) throws SQLException {
        Invoice inv = new Invoice();
        inv.setId(rs.getLong("id"));
        inv.setInvoiceNumber(rs.getString("invoice_number"));
        long sessionId = rs.getLong("session_id");
        if (!rs.wasNull()) inv.setSessionId(sessionId);
        long clientId = rs.getLong("client_id");
        if (!rs.wasNull()) inv.setClientId(clientId);
        inv.setClientEmail(rs.getString("client_email"));
        Date issue = rs.getDate("issue_date");
        if (issue != null) inv.setIssueDate(issue.toLocalDate());
        inv.setHours(rs.getInt("hours"));
        inv.setHourlyRate(rs.getBigDecimal("hourly_rate"));
        inv.setSubtotal(rs.getBigDecimal("subtotal"));
        inv.setDiscountRate(rs.getBigDecimal("discount_rate"));
        inv.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        inv.setTotal(rs.getBigDecimal("total"));
        inv.setPaidAmount(rs.getBigDecimal("paid_amount"));
        String status = rs.getString("status");
        if (status != null) inv.setStatus(PaymentStatus.valueOf(status));
        Date payment = rs.getDate("payment_date");
        if (payment != null) inv.setPaymentDate(payment.toLocalDate());
        inv.setDescription(rs.getString("description"));
        return inv;
    }

    public List<Invoice> findBySessionId(Long sessionId) {
        return queryByColumn("session_id", sessionId);
    }

    public List<Invoice> findByClientId(Long clientId) {
        return queryByColumn("client_id", clientId);
    }

    private List<Invoice> queryByColumn(String column, Long value) {
        String sql = "SELECT * FROM session_invoices WHERE " + column + " = ? ORDER BY issue_date DESC, id DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                List<Invoice> list = new ArrayList<>();
                while (rs.next()) list.add(mapResultSetToEntity(rs));
                return list;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error querying invoices by " + column, e);
        }
    }
}
